"""Main application entry point with dependency injection"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import get_settings
from app.infrastructure.gemini_service import GeminiImageService
from app.infrastructure.style_provider import CoupleStyleProvider
from app.application.use_cases import (
    GenerateFromTextUseCase,
    GenerateFromImageUseCase,
    GetStylesUseCase
)
from app.presentation.routes import create_router


# Load configuration
settings = get_settings()

# Initialize infrastructure
gemini_service = GeminiImageService(api_key=settings.GOOGLE_API_KEY)
style_provider = CoupleStyleProvider()

# Initialize use cases
text_use_case = GenerateFromTextUseCase(gemini_service, style_provider)
image_use_case = GenerateFromImageUseCase(gemini_service, style_provider)
styles_use_case = GetStylesUseCase(style_provider)

# Create FastAPI app
app = FastAPI(
    title="ImgGen - Couple Image Generator",
    version="2.0.0",
    description="Generate couple images with Gemini 2.5 Flash Image"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include router
app.include_router(create_router(text_use_case, image_use_case, styles_use_case))


@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "ImgGen - Couple Image Generator",
        "version": "2.0.0",
        "endpoints": {
            "text_generation": "/api/generate/text",
            "image_editing": "/api/generate/edit",
            "styles": "/api/styles",
            "docs": "/docs"
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host=settings.HOST, port=settings.PORT)

