"""API routes"""
import base64
from io import BytesIO
from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from PIL import Image
from .schemas import TextGenerationRequest, GenerationResponse
from ..application.use_cases import (
    GenerateFromTextUseCase,
    GenerateFromImageUseCase,
    GetStylesUseCase
)


def create_router(
    text_use_case: GenerateFromTextUseCase,
    image_use_case: GenerateFromImageUseCase,
    styles_use_case: GetStylesUseCase
) -> APIRouter:
    """Create API router with dependency injection"""
    
    router = APIRouter(prefix="/api", tags=["Generation"])
    
    @router.post("/generate/text", response_model=GenerationResponse)
    async def generate_from_text(request: TextGenerationRequest):
        """Generate image from text prompt only"""
        result = text_use_case.execute(request.prompt, request.style)
        
        if not result.success:
            raise HTTPException(status_code=500, detail=result.error)
        
        return GenerationResponse(
            success=True,
            image_base64=base64.b64encode(result.image_data).decode(),
            prompt_used=result.prompt_used
        )
    
    @router.post("/generate/edit", response_model=GenerationResponse)
    async def generate_from_images(
        prompt: str = Form(..., min_length=10),
        style: str = Form(default="romantic"),
        images: list[UploadFile] = File(...)
    ):
        """Generate/edit image from prompt + uploaded images"""
        try:
            # Load uploaded images
            pil_images = []
            for img_file in images:
                img_data = await img_file.read()
                pil_images.append(Image.open(BytesIO(img_data)))
            
            # Execute use case
            result = image_use_case.execute(prompt, pil_images, style)
            
            if not result.success:
                raise HTTPException(status_code=500, detail=result.error)
            
            return GenerationResponse(
                success=True,
                image_base64=base64.b64encode(result.image_data).decode(),
                prompt_used=result.prompt_used
            )
            
        except Exception as e:
            raise HTTPException(status_code=400, detail=str(e))
    
    @router.get("/styles")
    async def get_styles():
        """Get available styles"""
        return styles_use_case.execute()
    
    @router.get("/health")
    async def health():
        """Health check"""
        return {"status": "healthy", "version": "2.0.0"}
    
    return router

