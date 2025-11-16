import httpx
from typing import Dict, Any, Optional
from datetime import datetime, timedelta
from ..core.config import settings
from ..core.logging import get_logger

logger = get_logger(__name__)


class OpenMeteoClient:
    """Client for Open-Meteo weather API"""
    
    def __init__(self):
        self.base_url = settings.openmeteo_base_url
        self.client = httpx.AsyncClient(timeout=30.0)
    
    async def get_weather_forecast(
        self, 
        lat: float, 
        lng: float, 
        start_time: datetime,
        end_time: Optional[datetime] = None
    ) -> Dict[str, Any]:
        """
        Get weather forecast for a specific location and time range
        
        Args:
            lat: Latitude
            lng: Longitude  
            start_time: Start time for forecast
            end_time: End time for forecast (defaults to start_time + 3 hours)
            
        Returns:
            Weather data including conditions, temperature, precipitation
        """
        if end_time is None:
            end_time = start_time + timedelta(hours=3)
            
        try:
            # Format dates for API
            start_date = start_time.strftime("%Y-%m-%d")
            end_date = end_time.strftime("%Y-%m-%d")
            
            params = {
                "latitude": lat,
                "longitude": lng,
                "hourly": "temperature_2m,precipitation,weathercode,windspeed_10m,cloudcover",
                "start_date": start_date,
                "end_date": end_date,
                "timezone": "auto"
            }
            
            response = await self.client.get(f"{self.base_url}/forecast", params=params)
            response.raise_for_status()
            
            data = response.json()
            
            # Process the weather data to determine conditions
            weather_info = self._process_weather_data(data, start_time, end_time)
            
            logger.info("Weather forecast retrieved", 
                       lat=lat, lng=lng, 
                       start_time=start_time.isoformat(),
                       weather_ok=weather_info.get("weather_ok"))
            
            return weather_info
            
        except httpx.RequestError as e:
            logger.error("Weather API request failed", error=str(e))
            # Return neutral weather if API fails
            return {
                "weather_ok": 0.5,
                "temperature": None,
                "precipitation": None,
                "conditions": "unknown",
                "error": str(e)
            }
    
    def _process_weather_data(
        self, 
        data: Dict[str, Any], 
        start_time: datetime, 
        end_time: datetime
    ) -> Dict[str, Any]:
        """Process raw weather data to determine if conditions are good for dating"""
        
        hourly = data.get("hourly", {})
        times = hourly.get("time", [])
        temperatures = hourly.get("temperature_2m", [])
        precipitations = hourly.get("precipitation", [])
        weather_codes = hourly.get("weathercode", [])
        wind_speeds = hourly.get("windspeed_10m", [])
        
        if not times:
            return {"weather_ok": 0.5, "conditions": "unknown"}
        
        # Find relevant time indices
        relevant_data = []
        for i, time_str in enumerate(times):
            time_dt = datetime.fromisoformat(time_str.replace('Z', '+00:00'))
            if start_time <= time_dt <= end_time:
                relevant_data.append({
                    "time": time_dt,
                    "temperature": temperatures[i] if i < len(temperatures) else None,
                    "precipitation": precipitations[i] if i < len(precipitations) else 0,
                    "weather_code": weather_codes[i] if i < len(weather_codes) else None,
                    "wind_speed": wind_speeds[i] if i < len(wind_speeds) else 0,
                })
        
        if not relevant_data:
            return {"weather_ok": 0.5, "conditions": "unknown"}
        
        # Calculate weather score
        avg_temp = sum(d["temperature"] for d in relevant_data if d["temperature"]) / len(relevant_data)
        max_precipitation = max(d["precipitation"] for d in relevant_data)
        avg_wind = sum(d["wind_speed"] for d in relevant_data) / len(relevant_data)
        
        # Determine weather conditions (simplified scoring)
        weather_score = 1.0  # Start with perfect score
        
        # Temperature penalty (too cold or too hot)
        if avg_temp < 10 or avg_temp > 35:
            weather_score -= 0.3
        elif avg_temp < 15 or avg_temp > 30:
            weather_score -= 0.1
            
        # Precipitation penalty
        if max_precipitation > 5:  # Heavy rain
            weather_score -= 0.5
        elif max_precipitation > 1:  # Light rain
            weather_score -= 0.2
            
        # Wind penalty
        if avg_wind > 15:  # Strong wind
            weather_score -= 0.2
        
        weather_score = max(0.0, min(1.0, weather_score))
        
        # Determine conditions string
        if weather_score >= 0.8:
            conditions = "excellent"
        elif weather_score >= 0.6:
            conditions = "good"
        elif weather_score >= 0.4:
            conditions = "fair"
        else:
            conditions = "poor"
        
        return {
            "weather_ok": weather_score,
            "temperature": avg_temp,
            "precipitation": max_precipitation,
            "wind_speed": avg_wind,
            "conditions": conditions,
            "raw_data": relevant_data
        }
    
    async def close(self):
        """Close the HTTP client"""
        await self.client.aclose()
