"""
Application Use Cases - Business logic orchestration.
Following Single Responsibility Principle: each use case handles one business operation.
Following Dependency Inversion: depends on domain interfaces, not implementations.
"""
from io import BytesIO
from typing import List
from PIL import Image

from app.domain.entities import ClassificationResult, ImageCategory, ImageData
from app.domain.interfaces import IImageClassifier, IQualityDetector, IMetadataExtractor


class ClassifyImageUseCase:
    """
    Use case for classifying a single image.
    Orchestrates classifier, quality detector, and metadata extractor.
    
    Dependencies are injected via constructor (Dependency Injection pattern).
    """
    
    def __init__(
        self,
        classifier: IImageClassifier,
        quality_detector: IQualityDetector,
        metadata_extractor: IMetadataExtractor
    ):
        self._classifier = classifier
        self._quality_detector = quality_detector
        self._metadata_extractor = metadata_extractor
    
    async def execute(self, image_data: ImageData) -> ClassificationResult:
        """
        Execute the classification use case.
        
        Args:
            image_data: Image data to classify
            
        Returns:
            ClassificationResult with category, confidence, and metadata
            
        Raises:
            ValueError: If image cannot be processed
        """
        # Load image
        try:
            image = Image.open(BytesIO(image_data.content))
            if image.mode != 'RGB':
                image = image.convert('RGB')
        except Exception as e:
            raise ValueError(f"Failed to load image: {str(e)}")
        
        # Detect quality first (fast operation)
        quality_score, is_low_quality = await self._quality_detector.detect_quality(image)
        blur_score = await self._quality_detector.calculate_blur_score(image)
        
        # Extract metadata
        metadata = await self._metadata_extractor.extract(image, blur_score, is_low_quality)
        
        # If low quality, classify as noise
        if is_low_quality:
            return ClassificationResult(
                category=ImageCategory.NOISE,
                confidence=1.0 - quality_score,
                should_delete=True,
                quality_score=quality_score,
                metadata=metadata
            )
        
        # Classify the image
        category, confidence = await self._classifier.classify(image)
        
        # Determine if should delete (noise category or very low confidence)
        should_delete = (category == ImageCategory.NOISE) or (confidence < 0.3)
        
        return ClassificationResult(
            category=category,
            confidence=confidence,
            should_delete=should_delete,
            quality_score=quality_score,
            metadata=metadata
        )


class ClassifyBatchUseCase:
    """
    Use case for classifying multiple images in batch.
    Reuses ClassifyImageUseCase for each image (Don't Repeat Yourself).
    """
    
    def __init__(self, classify_use_case: ClassifyImageUseCase):
        self._classify_use_case = classify_use_case
    
    async def execute(self, images_data: List[ImageData]) -> List[ClassificationResult]:
        """
        Execute batch classification.
        
        Args:
            images_data: List of image data to classify
            
        Returns:
            List of ClassificationResult objects
        """
        results = []
        for image_data in images_data:
            try:
                result = await self._classify_use_case.execute(image_data)
                results.append(result)
            except Exception as e:
                # Log error but continue processing other images
                # In production, would use proper logging
                print(f"Error classifying {image_data.filename}: {str(e)}")
                continue
        
        return results


class HealthCheckUseCase:
    """Use case for checking system health."""
    
    def __init__(self, classifier: IImageClassifier):
        self._classifier = classifier
    
    async def execute(self) -> dict:
        """
        Check if the system is ready to process images.
        
        Returns:
            Dictionary with health status
        """
        model_ready = await self._classifier.is_ready()
        return {
            "status": "healthy" if model_ready else "unhealthy",
            "model_loaded": model_ready
        }


