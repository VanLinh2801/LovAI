"""Domain entities"""
from dataclasses import dataclass
from typing import Optional
from PIL import Image


@dataclass
class GenerationRequest:
    """Request for image generation"""
    prompt: str
    style: Optional[str] = None
    images: Optional[list[Image.Image]] = None


@dataclass
class GenerationResult:
    """Result of image generation"""
    image_data: bytes
    prompt_used: str
    success: bool = True
    error: Optional[str] = None

