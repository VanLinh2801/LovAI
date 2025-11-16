import httpx
from typing import Dict, Any, List, Optional
from ..core.config import settings
from ..core.logging import get_logger

logger = get_logger(__name__)


class GeoapifyClient:
    """Client for Geoapify Places API"""
    
    def __init__(self):
        self.base_url = settings.geoapify_base_url
        self.api_key = settings.geoapify_api_key
        self.client = httpx.AsyncClient(timeout=30.0)
    
    async def search_places(
        self,
        lat: float,
        lng: float,
        radius_km: float = 5.0,
        place_types: List[str] = None,
        limit: int = 20
    ) -> List[Dict[str, Any]]:
        """
        Search for places near a location
        
        Args:
            lat: Latitude
            lng: Longitude
            radius_km: Search radius in kilometers
            place_types: List of place types to search for
            limit: Maximum number of results
            
        Returns:
            List of place information
        """
        if not self.api_key:
            logger.error("Geoapify API key not configured")
            return []
            
        if place_types is None:
            place_types = ["restaurant", "cafe", "bar", "entertainment", "tourism"]
        
        try:
            places = []
            
            # Search for each place type
            for place_type in place_types:
                type_places = await self._search_by_type(
                    lat, lng, radius_km, place_type, limit // len(place_types)
                )
                places.extend(type_places)
            
            # Remove duplicates and sort by distance
            unique_places = self._deduplicate_places(places)
            unique_places.sort(key=lambda x: x.get("distance_km", float("inf")))
            
            logger.info("Places search completed", 
                       lat=lat, lng=lng, 
                       radius_km=radius_km,
                       total_found=len(unique_places))
            
            return unique_places[:limit]
            
        except Exception as e:
            logger.error("Places search failed", error=str(e))
            return []
    
    async def _search_by_type(
        self, 
        lat: float, 
        lng: float, 
        radius_km: float, 
        place_type: str, 
        limit: int
    ) -> List[Dict[str, Any]]:
        """Search for places of a specific type"""
        
        try:
            params = {
                "categories": place_type,
                "filter": f"circle:{lng},{lat},{radius_km * 1000}",  # Convert km to meters
                "bias": f"proximity:{lng},{lat}",
                "limit": min(limit, 50),  # API limit
                "apiKey": self.api_key
            }
            
            response = await self.client.get(f"{self.base_url}/places", params=params)
            response.raise_for_status()
            
            data = response.json()
            features = data.get("features", [])
            
            places = []
            for feature in features:
                place_info = self._extract_place_info(feature, lat, lng)
                if place_info:
                    places.append(place_info)
            
            return places
            
        except httpx.RequestError as e:
            logger.error("Places API request failed", place_type=place_type, error=str(e))
            return []
    
    def _extract_place_info(self, feature: Dict[str, Any], ref_lat: float, ref_lng: float) -> Optional[Dict[str, Any]]:
        """Extract relevant information from a place feature"""
        
        properties = feature.get("properties", {})
        geometry = feature.get("geometry", {})
        coordinates = geometry.get("coordinates", [])
        
        if len(coordinates) < 2:
            return None
        
        place_lng, place_lat = coordinates[0], coordinates[1]
        
        # Calculate distance
        distance_km = self._calculate_distance(ref_lat, ref_lng, place_lat, place_lng)
        
        # Extract place information
        place_info = {
            "place_id": properties.get("place_id", ""),
            "name": properties.get("name", "Unknown"),
            "address": self._format_address(properties),
            "lat": place_lat,
            "lng": place_lng,
            "distance_km": distance_km,
            "categories": properties.get("categories", []),
            "rating": self._extract_rating(properties),
            "price_level": self._extract_price_level(properties),
            "phone": properties.get("contact", {}).get("phone", ""),
            "website": properties.get("contact", {}).get("website", ""),
            "opening_hours": properties.get("opening_hours", {}),
            "source": "geoapify"
        }
        
        return place_info
    
    def _format_address(self, properties: Dict[str, Any]) -> str:
        """Format address from properties"""
        address_parts = []
        
        if properties.get("housenumber"):
            address_parts.append(properties["housenumber"])
        if properties.get("street"):
            address_parts.append(properties["street"])
        if properties.get("city"):
            address_parts.append(properties["city"])
        if properties.get("postcode"):
            address_parts.append(properties["postcode"])
            
        return ", ".join(address_parts) or properties.get("formatted", "")
    
    def _extract_rating(self, properties: Dict[str, Any]) -> Optional[float]:
        """Extract rating from properties"""
        # Geoapify might not always have ratings, try different fields
        rating = properties.get("rating")
        if rating:
            return float(rating)
        return None
    
    def _extract_price_level(self, properties: Dict[str, Any]) -> int:
        """Extract price level (1-4 scale)"""
        # This is a simplified implementation as Geoapify doesn't have standard price levels
        categories = properties.get("categories", [])
        
        # Simple heuristic based on categories
        if any("fine_dining" in cat or "luxury" in cat for cat in categories):
            return 4
        elif any("restaurant" in cat for cat in categories):
            return 3
        elif any("cafe" in cat or "fast_food" in cat for cat in categories):
            return 2
        else:
            return 1
    
    def _calculate_distance(self, lat1: float, lng1: float, lat2: float, lng2: float) -> float:
        """Calculate distance between two points using Haversine formula"""
        import math
        
        R = 6371  # Earth's radius in km
        
        dlat = math.radians(lat2 - lat1)
        dlng = math.radians(lng2 - lng1)
        
        a = (math.sin(dlat / 2) * math.sin(dlat / 2) +
             math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) *
             math.sin(dlng / 2) * math.sin(dlng / 2))
        
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        distance = R * c
        
        return round(distance, 2)
    
    def _deduplicate_places(self, places: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Remove duplicate places based on name and location proximity"""
        
        unique_places = []
        seen_places = set()
        
        for place in places:
            # Create a key based on name and approximate location
            name = place.get("name", "").lower().strip()
            lat_rounded = round(place.get("lat", 0), 4)  # ~10m precision
            lng_rounded = round(place.get("lng", 0), 4)
            
            key = f"{name}_{lat_rounded}_{lng_rounded}"
            
            if key not in seen_places:
                seen_places.add(key)
                unique_places.append(place)
        
        return unique_places
    
    async def close(self):
        """Close the HTTP client"""
        await self.client.aclose()
