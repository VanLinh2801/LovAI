import json
import hashlib
from datetime import datetime, timedelta
from typing import Any, Dict, Optional
from sqlalchemy.orm import Session
from sqlalchemy import and_
from ..models.cache import PlaceCache, WeatherCache
from ..core.config import settings
from ..core.logging import get_logger

logger = get_logger(__name__)


class CacheService:
    """Service for managing cache operations"""
    
    def __init__(self, db: Session):
        self.db = db
        self.default_ttl = settings.cache_ttl_seconds
    
    async def get_place_cache(self, key: str) -> Optional[Dict[str, Any]]:
        """Get cached place data"""
        try:
            cache_entry = self.db.query(PlaceCache).filter(
                and_(
                    PlaceCache.key == key,
                    PlaceCache.expires_at > datetime.utcnow()
                )
            ).first()
            
            if cache_entry:
                logger.debug("Cache hit for places", key=key)
                return cache_entry.payload_jsonb
            
            logger.debug("Cache miss for places", key=key)
            return None
            
        except Exception as e:
            logger.error("Error getting place cache", key=key, error=str(e))
            return None
    
    async def set_place_cache(self, key: str, data: Dict[str, Any], ttl_seconds: Optional[int] = None) -> None:
        """Set cached place data"""
        try:
            ttl = ttl_seconds or self.default_ttl
            expires_at = datetime.utcnow() + timedelta(seconds=ttl)
            
            # Delete existing entry if it exists
            self.db.query(PlaceCache).filter(PlaceCache.key == key).delete()
            
            # Create new cache entry
            cache_entry = PlaceCache(
                key=key,
                payload_jsonb=data,
                expires_at=expires_at
            )
            
            self.db.add(cache_entry)
            self.db.commit()
            
            logger.debug("Cache set for places", key=key, ttl=ttl)
            
        except Exception as e:
            logger.error("Error setting place cache", key=key, error=str(e))
            self.db.rollback()
    
    async def get_weather_cache(self, key: str) -> Optional[Dict[str, Any]]:
        """Get cached weather data"""
        try:
            cache_entry = self.db.query(WeatherCache).filter(
                and_(
                    WeatherCache.key == key,
                    WeatherCache.expires_at > datetime.utcnow()
                )
            ).first()
            
            if cache_entry:
                logger.debug("Cache hit for weather", key=key)
                return cache_entry.payload_jsonb
            
            logger.debug("Cache miss for weather", key=key)
            return None
            
        except Exception as e:
            logger.error("Error getting weather cache", key=key, error=str(e))
            return None
    
    async def set_weather_cache(self, key: str, data: Dict[str, Any], ttl_seconds: Optional[int] = None) -> None:
        """Set cached weather data"""
        try:
            ttl = ttl_seconds or self.default_ttl
            expires_at = datetime.utcnow() + timedelta(seconds=ttl)
            
            # Delete existing entry if it exists
            self.db.query(WeatherCache).filter(WeatherCache.key == key).delete()
            
            # Create new cache entry
            cache_entry = WeatherCache(
                key=key,
                payload_jsonb=data,
                expires_at=expires_at
            )
            
            self.db.add(cache_entry)
            self.db.commit()
            
            logger.debug("Cache set for weather", key=key, ttl=ttl)
            
        except Exception as e:
            logger.error("Error setting weather cache", key=key, error=str(e))
            self.db.rollback()
    
    def generate_place_cache_key(
        self, 
        lat: float, 
        lng: float, 
        radius_km: float, 
        place_types: list, 
        filters: Dict[str, Any] = None
    ) -> str:
        """Generate cache key for place search"""
        key_data = {
            "lat": round(lat, 4),
            "lng": round(lng, 4),
            "radius_km": radius_km,
            "place_types": sorted(place_types),
            "filters": filters or {}
        }
        
        key_string = json.dumps(key_data, sort_keys=True)
        return hashlib.md5(key_string.encode()).hexdigest()
    
    def generate_weather_cache_key(
        self, 
        lat: float, 
        lng: float, 
        start_time: datetime,
        end_time: datetime
    ) -> str:
        """Generate cache key for weather data"""
        key_data = {
            "lat": round(lat, 4),
            "lng": round(lng, 4),
            "start_time": start_time.isoformat(),
            "end_time": end_time.isoformat()
        }
        
        key_string = json.dumps(key_data, sort_keys=True)
        return hashlib.md5(key_string.encode()).hexdigest()
    
    async def cleanup_expired_cache(self) -> None:
        """Remove expired cache entries"""
        try:
            now = datetime.utcnow()
            
            # Clean up place cache
            place_count = self.db.query(PlaceCache).filter(PlaceCache.expires_at <= now).count()
            self.db.query(PlaceCache).filter(PlaceCache.expires_at <= now).delete()
            
            # Clean up weather cache
            weather_count = self.db.query(WeatherCache).filter(WeatherCache.expires_at <= now).count()
            self.db.query(WeatherCache).filter(WeatherCache.expires_at <= now).delete()
            
            self.db.commit()
            
            logger.info("Cache cleanup completed", 
                       place_entries_removed=place_count,
                       weather_entries_removed=weather_count)
            
        except Exception as e:
            logger.error("Error during cache cleanup", error=str(e))
            self.db.rollback()
