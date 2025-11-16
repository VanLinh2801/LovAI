"""Gemini 2.5 Flash Image service implementation"""
import os
from io import BytesIO
from google import genai
from PIL import Image
from ..domain.entities import GenerationRequest, GenerationResult
from ..domain.interfaces import IImageGenerator


class GeminiImageService(IImageGenerator):
    """Gemini image generation service"""
    
    def __init__(self, api_key: str):
        self._client = genai.Client(api_key=api_key)
        self._model = "gemini-2.5-flash-image"
    
    def generate(self, request: GenerationRequest) -> GenerationResult:
        """Generate image using Gemini API"""
        try:
            # Prepare contents for API call
            contents = [request.prompt]
            
            # Add images if provided (for image editing mode)
            if request.images:
                contents.extend(request.images)
            
            # Call Gemini API
            response = self._client.models.generate_content(
                model=self._model,
                contents=contents,
            )
            
            # Extract image from response
            for part in response.candidates[0].content.parts:
                if part.inline_data is not None:
                    return GenerationResult(
                        image_data=part.inline_data.data,
                        prompt_used=request.prompt,
                        success=True
                    )
            
            return GenerationResult(
                image_data=b"",
                prompt_used=request.prompt,
                success=False,
                error="No image data in response"
            )
            
        except Exception as e:
            return GenerationResult(
                image_data=b"",
                prompt_used=request.prompt,
                success=False,
                error=str(e)
            )

