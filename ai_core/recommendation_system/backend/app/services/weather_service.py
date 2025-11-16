from datetime import datetime, timedelta
from typing import Dict, Any, Optional
from sqlalchemy.orm import Session
from ..clients.weather_client import OpenMeteoClient
from .cache_service import CacheService
from ..core.logging import get_logger

logger = get_logger(__name__)


class WeatherService:
    """Service for weather data management"""
    
    def __init__(self, db: Session):
        self.db = db
        self.weather_client = OpenMeteoClient()
        self.cache_service = CacheService(db)
    
    async def get_weather_forecast(
        self,
        lat: float,
        lng: float,
        start_time: datetime,
        end_time: Optional[datetime] = None
    ) -> Dict[str, Any]:
        """
        Get weather forecast with caching
        
        Args:
            lat: Latitude
            lng: Longitude
            start_time: Start time for forecast
            end_time: End time for forecast
            
        Returns:
            Weather data with weather_ok score
        """
        if end_time is None:
            end_time = start_time + timedelta(hours=3)
        
        # Generate cache key
        cache_key = self.cache_service.generate_weather_cache_key(
            lat, lng, start_time, end_time
        )
        
        # Try to get from cache first
        cached_weather = await self.cache_service.get_weather_cache(cache_key)
        if cached_weather:
            logger.info("Weather data retrieved from cache", lat=lat, lng=lng)
            return cached_weather
        
        # Fetch from API
        weather_data = await self.weather_client.get_weather_forecast(
            lat, lng, start_time, end_time
        )
        
        # Cache the result (with shorter TTL for weather as it changes frequently)
        await self.cache_service.set_weather_cache(
            cache_key, weather_data, ttl_seconds=1800  # 30 minutes
        )
        
        logger.info("Weather data retrieved from API", 
                   lat=lat, lng=lng,
                   weather_ok=weather_data.get("weather_ok"))
        
        return weather_data
    
    def evaluate_weather_conditions(self, weather_data: Dict[str, Any]) -> float:
        """
        Evaluate weather conditions and return a score
        
        Args:
            weather_data: Weather data from get_weather_forecast
            
        Returns:
            Weather score between 0.0 and 1.0
        """
        return weather_data.get("weather_ok", 0.5)
    
    def is_weather_suitable_for_outdoor(self, weather_data: Dict[str, Any]) -> bool:
        """Check if weather is suitable for outdoor activities"""
        weather_score = self.evaluate_weather_conditions(weather_data)
        return weather_score >= 0.6
    
    def is_weather_suitable_for_indoor(self, weather_data: Dict[str, Any]) -> bool:
        """Check if weather favors indoor activities"""
        weather_score = self.evaluate_weather_conditions(weather_data)
        return weather_score < 0.4
    
    def get_weather_bonus(self, weather_data: Dict[str, Any]) -> float:
        """
        Get weather bonus for recommendation scoring
        
        Returns:
            1.0 for good weather, 0.5 for neutral, 0.0 for bad weather
        """
        weather_score = self.evaluate_weather_conditions(weather_data)
        
        if weather_score >= 0.7:
            return 1.0
        elif weather_score >= 0.4:
            return 0.5
        else:
            return 0.0
    
    async def close(self):
        """Close weather client"""
        await self.weather_client.close()
