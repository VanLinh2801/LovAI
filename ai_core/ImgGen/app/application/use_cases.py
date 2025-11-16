"""Application use cases"""
from PIL import Image
from ..domain.entities import GenerationRequest, GenerationResult
from ..domain.interfaces import IImageGenerator, IStyleProvider


class GenerateFromTextUseCase:
    """Use case: Generate image from text prompt only"""
    
    def __init__(self, generator: IImageGenerator, style_provider: IStyleProvider):
        self._generator = generator
        self._style_provider = style_provider
    
    def execute(self, prompt: str, style: str = "romantic") -> GenerationResult:
        """Execute text-to-image generation"""
        full_prompt = self._style_provider.build_prompt(prompt, style)
        request = GenerationRequest(prompt=full_prompt, style=style)
        return self._generator.generate(request)


class GenerateFromImageUseCase:
    """Use case: Generate/edit image from prompt + input images"""
    
    def __init__(self, generator: IImageGenerator, style_provider: IStyleProvider):
        self._generator = generator
        self._style_provider = style_provider
    
    def execute(
        self, 
        prompt: str, 
        images: list[Image.Image],
        style: str = "romantic"
    ) -> GenerationResult:
        """Execute image editing generation"""
        full_prompt = self._style_provider.build_prompt(prompt, style)
        request = GenerationRequest(prompt=full_prompt, style=style, images=images)
        return self._generator.generate(request)


class GetStylesUseCase:
    """Use case: Get available styles"""
    
    def __init__(self, style_provider: IStyleProvider):
        self._style_provider = style_provider
    
    def execute(self) -> list[dict]:
        """Get all available styles"""
        return self._style_provider.get_styles()

