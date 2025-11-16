"""
Dependency Injection Container.
Following Dependency Inversion Principle: wire up concrete implementations.
Provides singleton instances of services.
"""
from functools import lru_cache

from app.config.settings import settings
from app.domain.interfaces import IImageClassifier, IQualityDetector, IMetadataExtractor
from app.infrastructure.classifiers.coca_classifier import CoCaImageClassifier
from app.infrastructure.classifiers.demo_classifier import DemoImageClassifier
from app.infrastructure.detectors.quality_detector import OpenCVQualityDetector, MetadataExtractorImpl
from app.application.use_cases import ClassifyImageUseCase, ClassifyBatchUseCase, HealthCheckUseCase


class DependencyContainer:
    """
    Container for managing application dependencies.
    Implements singleton pattern for shared services.
    """
    
    _instance = None
    _classifier: IImageClassifier = None
    _quality_detector: IQualityDetector = None
    _metadata_extractor: IMetadataExtractor = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance
    
    async def get_classifier(self) -> IImageClassifier:
        """Get or create classifier instance with fallback to demo mode."""
        if self._classifier is None:
            # Try to load CoCa model first
            try:
                self._classifier = CoCaImageClassifier(
                    model_name=settings.model_name,
                    pretrained=settings.model_pretrained
                )
                await self._classifier.load_model()
            except Exception as e:
                # Fall back to demo classifier if CoCa fails
                print(f"\n⚠️  CoCa model loading failed: {str(e)[:100]}")
                print("Falling back to Demo Classifier for testing...\n")
                self._classifier = DemoImageClassifier()
                await self._classifier.load_model()
        return self._classifier
    
    async def get_quality_detector(self) -> IQualityDetector:
        """Get or create quality detector instance."""
        if self._quality_detector is None:
            self._quality_detector = OpenCVQualityDetector(
                blur_threshold=settings.blur_threshold,
                min_resolution=settings.min_image_resolution
            )
        return self._quality_detector
    
    async def get_metadata_extractor(self) -> IMetadataExtractor:
        """Get or create metadata extractor instance."""
        if self._metadata_extractor is None:
            self._metadata_extractor = MetadataExtractorImpl()
        return self._metadata_extractor
    
    async def get_classify_use_case(self) -> ClassifyImageUseCase:
        """Get classify image use case with all dependencies."""
        classifier = await self.get_classifier()
        quality_detector = await self.get_quality_detector()
        metadata_extractor = await self.get_metadata_extractor()
        
        return ClassifyImageUseCase(
            classifier=classifier,
            quality_detector=quality_detector,
            metadata_extractor=metadata_extractor
        )
    
    async def get_batch_use_case(self) -> ClassifyBatchUseCase:
        """Get batch classify use case."""
        classify_use_case = await self.get_classify_use_case()
        return ClassifyBatchUseCase(classify_use_case)
    
    async def get_health_check_use_case(self) -> HealthCheckUseCase:
        """Get health check use case."""
        classifier = await self.get_classifier()
        return HealthCheckUseCase(classifier)


# Global container instance
container = DependencyContainer()

