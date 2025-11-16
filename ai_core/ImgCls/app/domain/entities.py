"""
Domain entities - Core business objects with no external dependencies.
Following Single Responsibility Principle: each entity has one clear purpose.
"""
from dataclasses import dataclass
from enum import Enum
from typing import Optional


class ImageCategory(str, Enum):
    """Enumeration of supported image categories."""
    BOYFRIEND = "boyfriend"
    GIRLFRIEND = "girlfriend"
    COUPLE = "couple"
    LANDSCAPE = "landscape"
    NOISE = "noise"


@dataclass(frozen=True)
class ImageMetadata:
    """Value object representing image technical metadata."""
    width: int
    height: int
    format: str
    size_bytes: int
    blur_score: float
    is_blurry: bool
    
    @property
    def resolution(self) -> str:
        """Return formatted resolution string."""
        return f"{self.width}x{self.height}"


@dataclass(frozen=True)
class ClassificationResult:
    """
    Entity representing the result of image classification.
    Immutable to ensure data integrity.
    """
    category: ImageCategory
    confidence: float
    should_delete: bool
    quality_score: float
    metadata: ImageMetadata
    
    def __post_init__(self):
        """Validate business rules."""
        if not 0.0 <= self.confidence <= 1.0:
            raise ValueError("Confidence must be between 0 and 1")
        if not 0.0 <= self.quality_score <= 1.0:
            raise ValueError("Quality score must be between 0 and 1")


@dataclass
class ImageData:
    """Entity representing image data for processing."""
    filename: str
    content: bytes
    content_type: str


