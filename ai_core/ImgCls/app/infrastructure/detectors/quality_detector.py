"""
Image Quality Detector Implementation.
Implements IQualityDetector interface using OpenCV.
Detects blur, noise, and low quality images.
"""
import cv2
import numpy as np
from PIL import Image
from typing import Tuple

from app.domain.interfaces import IQualityDetector


class OpenCVQualityDetector(IQualityDetector):
    """
    Quality detector using OpenCV algorithms.
    Single Responsibility: focuses only on quality assessment.
    """
    
    def __init__(
        self,
        blur_threshold: float = 100.0,
        min_resolution: int = 100
    ):
        """
        Initialize quality detector.
        
        Args:
            blur_threshold: Laplacian variance threshold (lower = more blurry)
            min_resolution: Minimum width/height in pixels
        """
        self._blur_threshold = blur_threshold
        self._min_resolution = min_resolution
    
    async def detect_quality(self, image: Image.Image) -> Tuple[float, bool]:
        """
        Detect overall image quality.
        
        Args:
            image: PIL Image object
            
        Returns:
            Tuple of (quality_score, is_low_quality)
            quality_score: 0.0 (worst) to 1.0 (best)
            is_low_quality: True if image should be considered low quality
        """
        # Check resolution
        width, height = image.size
        if width < self._min_resolution or height < self._min_resolution:
            return 0.0, True
        
        # Calculate blur score
        blur_score = await self.calculate_blur_score(image)
        
        # Determine if blurry
        is_blurry = blur_score < self._blur_threshold
        
        # Normalize blur score to 0-1 range
        # Higher blur_score = sharper image = higher quality
        # Using sigmoid-like normalization
        normalized_score = min(1.0, blur_score / (self._blur_threshold * 2))
        
        # Check other quality factors
        quality_factors = [normalized_score]
        
        # Check aspect ratio (extremely distorted images)
        aspect_ratio = max(width, height) / min(width, height)
        if aspect_ratio > 10:  # Very extreme aspect ratio
            quality_factors.append(0.3)
        
        # Average quality score
        quality_score = sum(quality_factors) / len(quality_factors)
        
        # Consider low quality if score is below threshold
        is_low_quality = quality_score < 0.4 or is_blurry
        
        return quality_score, is_low_quality
    
    async def calculate_blur_score(self, image: Image.Image) -> float:
        """
        Calculate blur score using Laplacian variance method.
        Higher score = sharper image.
        
        Args:
            image: PIL Image object
            
        Returns:
            Blur score (higher is better)
        """
        # Convert PIL Image to OpenCV format
        image_array = np.array(image)
        
        # Convert to grayscale if needed
        if len(image_array.shape) == 3:
            gray = cv2.cvtColor(image_array, cv2.COLOR_RGB2GRAY)
        else:
            gray = image_array
        
        # Calculate Laplacian variance
        laplacian = cv2.Laplacian(gray, cv2.CV_64F)
        variance = laplacian.var()
        
        return float(variance)


class MetadataExtractorImpl:
    """
    Implementation of metadata extraction.
    Separated for Single Responsibility.
    """
    
    async def extract(self, image: Image.Image, blur_score: float, is_blurry: bool):
        """Extract technical metadata from image."""
        from app.domain.entities import ImageMetadata
        
        # Get image size in bytes (estimate for PIL Image)
        import io
        buffer = io.BytesIO()
        image.save(buffer, format=image.format or 'JPEG')
        size_bytes = buffer.tell()
        
        return ImageMetadata(
            width=image.width,
            height=image.height,
            format=image.format or 'UNKNOWN',
            size_bytes=size_bytes,
            blur_score=blur_score,
            is_blurry=is_blurry
        )


