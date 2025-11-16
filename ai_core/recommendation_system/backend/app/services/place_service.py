from typing import Dict, Any, List, Optional, Tuple
from sqlalchemy.orm import Session
from ..clients.places_client import GeoapifyClient
from .cache_service import CacheService
from ..core.config import settings
from ..core.logging import get_logger

logger = get_logger(__name__)


class PlaceService:
    """Service for place data management and filtering"""
    
    def __init__(self, db: Session):
        self.db = db
        self.places_client = GeoapifyClient()
        self.cache_service = CacheService(db)
    
    async def search_places(
        self,
        lat: float,
        lng: float,
        radius_km: float = None,
        place_types: List[str] = None,
        budget_range: Tuple[int, int] = None,
        open_now: bool = True,
        limit: int = 20
    ) -> List[Dict[str, Any]]:
        """
        Search for places with filtering and caching
        
        Args:
            lat: Latitude
            lng: Longitude
            radius_km: Search radius in km
            place_types: List of place types to search for
            budget_range: Tuple of (min_price_level, max_price_level)
            open_now: Whether to filter for currently open places
            limit: Maximum number of results
            
        Returns:
            List of filtered place information
        """
        if radius_km is None:
            radius_km = settings.default_radius_km
            
        if place_types is None:
            place_types = ["restaurant", "cafe", "bar", "entertainment"]
        
        # Generate cache key
        filters = {
            "budget_range": budget_range,
            "open_now": open_now,
            "limit": limit
        }
        cache_key = self.cache_service.generate_place_cache_key(
            lat, lng, radius_km, place_types, filters
        )
        
        # Try to get from cache first
        cached_places = await self.cache_service.get_place_cache(cache_key)
        if cached_places:
            logger.info("Places data retrieved from cache", 
                       lat=lat, lng=lng, count=len(cached_places))
            return cached_places
        
        # Fetch from API
        places = await self.places_client.search_places(
            lat, lng, radius_km, place_types, limit * 2  # Get more for filtering
        )
        
        # Apply filters
        filtered_places = self._apply_filters(
            places, budget_range, open_now
        )[:limit]
        
        # Enhance place data
        enhanced_places = self._enhance_place_data(filtered_places)
        
        # Cache the result
        await self.cache_service.set_place_cache(cache_key, enhanced_places)
        
        logger.info("Places data retrieved from API", 
                   lat=lat, lng=lng, 
                   total_found=len(places),
                   after_filtering=len(enhanced_places))
        
        return enhanced_places
    
    def _apply_filters(
        self,
        places: List[Dict[str, Any]], 
        budget_range: Optional[Tuple[int, int]], 
        open_now: bool
    ) -> List[Dict[str, Any]]:
        """Apply filters to places list"""
        
        filtered_places = places.copy()
        
        # Budget filter
        if budget_range:
            min_price, max_price = budget_range
            filtered_places = [
                place for place in filtered_places
                if min_price <= place.get("price_level", 2) <= max_price
            ]
        
        # Open now filter (simplified - would need real opening hours data)
        if open_now:
            # For MVP, we'll assume places are open during reasonable hours
            # In production, this would check actual opening hours
            pass
        
        return filtered_places
    
    def _enhance_place_data(self, places: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Enhance place data with additional computed fields"""
        
        enhanced_places = []
        
        for place in places:
            enhanced_place = place.copy()
            
            # Normalize rating to 0-1 scale
            rating = place.get("rating")
            if rating is not None:
                enhanced_place["normalized_rating"] = min(rating / 5.0, 1.0)
            else:
                enhanced_place["normalized_rating"] = 0.5  # Default rating
            
            # Calculate distance score
            distance_km = place.get("distance_km", 0)
            enhanced_place["distance_score"] = self._calculate_distance_score(distance_km)
            
            # Infer place category for affinity calculation
            enhanced_place["inferred_category"] = self._infer_place_category(place)
            
            # Generate Google Maps deeplink
            enhanced_place["maps_deeplink"] = self._generate_maps_deeplink(place)
            
            enhanced_places.append(enhanced_place)
        
        return enhanced_places
    
    def _calculate_distance_score(self, distance_km: float) -> float:
        """Calculate distance score using 1/(1+dist) formula"""
        return 1.0 / (1.0 + distance_km)
    
    def _infer_place_category(self, place: Dict[str, Any]) -> str:
        """Infer place category from categories and name"""
        categories = place.get("categories", [])
        name = place.get("name", "").lower()
        
        # Simple category inference
        if any("restaurant" in cat for cat in categories) or "restaurant" in name:
            return "restaurant"
        elif any("cafe" in cat for cat in categories) or "cafe" in name or "coffee" in name:
            return "cafe"
        elif any("bar" in cat for cat in categories) or "bar" in name or "pub" in name:
            return "bar"
        elif any("entertainment" in cat for cat in categories):
            return "entertainment"
        elif any("tourism" in cat for cat in categories):
            return "tourism"
        else:
            return "other"
    
    def _generate_maps_deeplink(self, place: Dict[str, Any]) -> str:
        """Generate Google Maps deeplink for the place"""
        lat = place.get("lat")
        lng = place.get("lng")
        name = place.get("name", "")
        
        if lat and lng:
            # Google Maps URL with coordinates and place name
            return f"https://www.google.com/maps/search/?api=1&query={lat},{lng}&query_place_id={name}"
        
        return ""
    
    def get_place_types_from_preferences(self, preferences: Dict[str, Any]) -> List[str]:
        """Extract place types from user/couple preferences"""
        
        # Default place types
        default_types = ["restaurant", "cafe", "bar", "entertainment"]
        
        if not preferences:
            return default_types
        
        # Extract preferred cuisines/types from preferences
        preferred_types = preferences.get("preferred_place_types", [])
        if preferred_types:
            return preferred_types
        
        # Fallback to cuisines
        preferred_cuisines = preferences.get("preferred_cuisines", [])
        if preferred_cuisines:
            # Map cuisines to place types
            return ["restaurant", "cafe"]  # Focus on food places
        
        return default_types
    
    def normalize_place_rating(self, rating: Optional[float]) -> float:
        """Normalize place rating to 0-1 scale"""
        if rating is None:
            return 0.5  # Default neutral rating
        return min(rating / 5.0, 1.0)
    
    async def close(self):
        """Close places client"""
        await self.places_client.close()
