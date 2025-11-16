"""
Domain interfaces (Ports) - Abstractions for external dependencies.
Following Interface Segregation Principle: small, focused interfaces.
Following Dependency Inversion Principle: depend on abstractions, not concretions.
"""
from abc import ABC, abstractmethod
from typing import Tuple
from PIL import Image

from app.domain.entities import ClassificationResult, ImageCategory, ImageMetadata


class IImageClassifier(ABC):
    """
    Interface for image classification models.
    Allows different classifier implementations (Open/Closed Principle).
    """
    
    @abstractmethod
    async def classify(self, image: Image.Image) -> Tuple[ImageCategory, float]:
        """
        Classify an image into a category.
        
        Args:
            image: PIL Image object
            
        Returns:
            Tuple of (category, confidence_score)
        """
        pass
    
    @abstractmethod
    async def is_ready(self) -> bool:
        """Check if the classifier is loaded and ready."""
        pass


class IQualityDetector(ABC):
    """
    Interface for image quality detection.
    Single Responsibility: only concerned with quality assessment.
    """
    
    @abstractmethod
    async def detect_quality(self, image: Image.Image) -> Tuple[float, bool]:
        """
        Detect image quality (blur, noise, etc).
        
        Args:
            image: PIL Image object
            
        Returns:
            Tuple of (quality_score, is_low_quality)
        """
        pass
    
    @abstractmethod
    async def calculate_blur_score(self, image: Image.Image) -> float:
        """Calculate blur score using Laplacian variance."""
        pass


class IMetadataExtractor(ABC):
    """
    Interface for extracting image metadata.
    Single Responsibility: metadata extraction only.
    """
    
    @abstractmethod
    async def extract(self, image: Image.Image, blur_score: float, is_blurry: bool) -> ImageMetadata:
        """
        Extract technical metadata from image.
        
        Args:
            image: PIL Image object
            blur_score: Pre-calculated blur score
            is_blurry: Whether image is blurry
            
        Returns:
            ImageMetadata object
        """
        pass


