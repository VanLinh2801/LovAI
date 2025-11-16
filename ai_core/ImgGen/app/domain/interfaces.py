"""Domain interfaces (abstractions)"""
from abc import ABC, abstractmethod
from .entities import GenerationRequest, GenerationResult


class IImageGenerator(ABC):
    """Interface for image generation service"""
    
    @abstractmethod
    def generate(self, request: GenerationRequest) -> GenerationResult:
        """Generate image from request"""
        pass


class IStyleProvider(ABC):
    """Interface for style management"""
    
    @abstractmethod
    def get_styles(self) -> list[dict]:
        """Get available styles"""
        pass
    
    @abstractmethod
    def build_prompt(self, base_prompt: str, style: str) -> str:
        """Build complete prompt with style"""
        pass

