"""
Data Transfer Objects (DTOs) for API requests and responses.
Using Pydantic for validation and serialization.
"""
from pydantic import BaseModel, Field
from typing import Optional


class ImageMetadataDTO(BaseModel):
    """DTO for image metadata."""
    resolution: str = Field(..., description="Image resolution (WxH)")
    width: int = Field(..., description="Image width in pixels")
    height: int = Field(..., description="Image height in pixels")
    format: str = Field(..., description="Image format")
    size_bytes: int = Field(..., description="Image size in bytes")
    blur_score: float = Field(..., description="Blur detection score")
    is_blurry: bool = Field(..., description="Whether image is blurry")
    
    class Config:
        json_schema_extra = {
            "example": {
                "resolution": "1920x1080",
                "width": 1920,
                "height": 1080,
                "format": "JPEG",
                "size_bytes": 524288,
                "blur_score": 150.5,
                "is_blurry": False
            }
        }


class ClassificationResponseDTO(BaseModel):
    """DTO for classification response."""
    filename: str = Field(..., description="Original filename")
    category: str = Field(..., description="Classified category")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Classification confidence")
    should_delete: bool = Field(..., description="Whether image should be deleted")
    quality_score: float = Field(..., ge=0.0, le=1.0, description="Overall quality score")
    metadata: ImageMetadataDTO = Field(..., description="Image technical metadata")
    
    class Config:
        json_schema_extra = {
            "example": {
                "filename": "photo.jpg",
                "category": "couple",
                "confidence": 0.95,
                "should_delete": False,
                "quality_score": 0.85,
                "metadata": {
                    "resolution": "1920x1080",
                    "width": 1920,
                    "height": 1080,
                    "format": "JPEG",
                    "size_bytes": 524288,
                    "blur_score": 150.5,
                    "is_blurry": False
                }
            }
        }


class BatchClassificationResponseDTO(BaseModel):
    """DTO for batch classification response."""
    total: int = Field(..., description="Total images processed")
    results: list[ClassificationResponseDTO] = Field(..., description="Classification results")


class HealthResponseDTO(BaseModel):
    """DTO for health check response."""
    model_config = {"protected_namespaces": ()}
    
    status: str = Field(..., description="Service status")
    model_loaded: bool = Field(..., description="Whether model is loaded")


class ErrorResponseDTO(BaseModel):
    """DTO for error responses."""
    detail: str = Field(..., description="Error message")
    
    class Config:
        json_schema_extra = {
            "example": {
                "detail": "Invalid image format"
            }
        }


class CategoriesResponseDTO(BaseModel):
    """DTO for categories list."""
    categories: list[str] = Field(..., description="List of supported categories")
    
    class Config:
        json_schema_extra = {
            "example": {
                "categories": ["boyfriend", "girlfriend", "couple", "landscape", "noise"]
            }
        }

