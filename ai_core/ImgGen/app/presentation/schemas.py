"""API request/response schemas"""
from pydantic import BaseModel, Field


class TextGenerationRequest(BaseModel):
    """Request for text-to-image generation"""
    prompt: str = Field(..., min_length=10, max_length=1000)
    style: str = Field(default="romantic")


class GenerationResponse(BaseModel):
    """Response for image generation"""
    success: bool
    image_base64: str | None = None
    prompt_used: str
    error: str | None = None

