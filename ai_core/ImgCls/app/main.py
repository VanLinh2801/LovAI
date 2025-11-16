"""
Main application entry point.
Sets up FastAPI application with all routes and middleware.
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from app.presentation.api.routes import router
from app.config.settings import settings
from app.config.dependencies import container


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Lifespan context manager for startup and shutdown events.
    Pre-loads the model during startup for faster first request.
    """
    # Startup: Load model
    print("Starting up application...")
    print(f"Loading CoCa model: {settings.model_name}")
    try:
        await container.get_classifier()
        print("Model loaded successfully!")
    except Exception as e:
        print(f"Warning: Failed to load model during startup: {e}")
    
    yield
    
    # Shutdown
    print("Shutting down application...")


# Create FastAPI application
app = FastAPI(
    title="AI Image Classification Service",
    description="Image classification using CoCa model to organize photos into albums",
    version="1.0.0",
    lifespan=lifespan
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(router)


@app.get("/", tags=["root"])
async def root():
    """Root endpoint with service information."""
    return {
        "service": "AI Image Classification Service",
        "version": "1.0.0",
        "model": settings.model_name,
        "categories": settings.categories_list,
        "docs": "/docs"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.api_host,
        port=settings.api_port,
        reload=settings.api_reload
    )


