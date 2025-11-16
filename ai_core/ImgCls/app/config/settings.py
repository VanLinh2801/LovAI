"""
Application configuration using Pydantic Settings.
Centralized configuration management with environment variable support.
"""
from typing import List
from pydantic_settings import BaseSettings
from pydantic import Field


class Settings(BaseSettings):
    """
    Application settings with validation.
    Follows Single Responsibility: only configuration management.
    """
    
    model_config = {
        "protected_namespaces": (),
        "env_file": "config.env",
        "env_file_encoding": "utf-8",
        "case_sensitive": False
    }
    
    # Model Configuration
    model_name: str = Field(
        default="coca_ViT-L-14",
        description="CoCa model name"
    )
    model_pretrained: str = Field(
        default="mscoco_finetuned_laion2B-s13B-b90k",
        description="Pretrained weights identifier"
    )
    
    # API Configuration
    api_host: str = Field(default="0.0.0.0", description="API host")
    api_port: int = Field(default=8000, description="API port")
    api_reload: bool = Field(default=False, description="Enable auto-reload")
    
    # Image Processing
    max_image_size_mb: int = Field(default=10, description="Max image size in MB")
    min_image_resolution: int = Field(default=100, description="Min image resolution")
    blur_threshold: float = Field(default=100.0, description="Blur detection threshold")
    
    # Categories
    categories: str = Field(
        default="boyfriend,girlfriend,couple,landscape,noise",
        description="Supported categories"
    )
    
    @property
    def categories_list(self) -> List[str]:
        """Get categories as list."""
        return [cat.strip() for cat in self.categories.split(",")]
    
    @property
    def max_image_size_bytes(self) -> int:
        """Get max image size in bytes."""
        return self.max_image_size_mb * 1024 * 1024


# Global settings instance
settings = Settings()

