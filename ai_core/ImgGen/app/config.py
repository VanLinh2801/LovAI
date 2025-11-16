"""Application configuration"""
import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings"""
    GOOGLE_API_KEY: str
    HOST: str = "0.0.0.0"
    PORT: int = 6001
    
    class Config:
        env_file = ".env"


def get_settings() -> Settings:
    """Get application settings"""
    return Settings()

