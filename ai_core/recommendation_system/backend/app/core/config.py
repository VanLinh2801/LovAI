from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    # App
    app_name: str = "LovAI Recommendation System"
    debug: bool = False
    version: str = "0.1.0"
    
    # Database
    database_url: str = "postgresql://postgres:password@localhost:5432/lovai_reco"
    
    # External APIs
    openmeteo_base_url: str = "https://api.open-meteo.com/v1"
    geoapify_api_key: Optional[str] = None
    geoapify_base_url: str = "https://api.geoapify.com/v2"
    
    # Firebase
    firebase_credentials_path: Optional[str] = None
    firebase_project_id: Optional[str] = None
    
    # Auth
    secret_key: str = "your-secret-key-change-in-production"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 30
    
    # Scheduler
    scheduler_enabled: bool = True
    daily_pref_job_hour: int = 6  # Run at 6 AM
    generate_reco_job_interval_minutes: int = 120  # Every 2 hours
    post_event_infer_job_hour: int = 23  # Run at 11 PM
    
    # Recommendation
    max_recommendations: int = 5
    default_radius_km: float = 10.0
    max_radius_km: float = 50.0
    cache_ttl_seconds: int = 3600  # 1 hour
    
    # Scoring weights
    affinity_weight: float = 0.35
    rating_weight: float = 0.30
    distance_weight: float = 0.20
    weather_weight: float = 0.15
    
    class Config:
        env_file = ".env"
        case_sensitive = False


settings = Settings()
