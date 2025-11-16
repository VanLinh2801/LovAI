from fastapi import FastAPI, Request, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from contextlib import asynccontextmanager
import uvicorn

from app.api import api_router
from app.core.config import settings
from app.core.logging import configure_logging, get_logger
from app.core.database import Base, engine
from app.core.scheduler import scheduler

# Configure logging
configure_logging(settings.debug)
logger = get_logger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan events"""
    
    # Startup
    logger.info("Starting LovAI Recommendation System", version=settings.version)
    
    try:
        # Create database tables
        Base.metadata.create_all(bind=engine)
        logger.info("Database tables created/verified")
        
        # Start scheduler
        await scheduler.start()
        logger.info("Scheduler started")
        
        logger.info("Application startup completed successfully")
        
    except Exception as e:
        logger.error("Failed to start application", error=str(e))
        raise
    
    yield
    
    # Shutdown
    logger.info("Shutting down application")
    
    try:
        # Stop scheduler
        await scheduler.stop()
        logger.info("Scheduler stopped")
        
        logger.info("Application shutdown completed")
        
    except Exception as e:
        logger.error("Error during application shutdown", error=str(e))


# Create FastAPI application
app = FastAPI(
    title=settings.app_name,
    version=settings.version,
    description="LovAI Recommendation System MVP - AI-powered dating recommendations",
    debug=settings.debug,
    lifespan=lifespan
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure appropriately for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Global exception handler
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """Global exception handler for unhandled errors"""
    
    logger.error("Unhandled exception",
                path=request.url.path,
                method=request.method,
                error=str(exc),
                exc_info=True)
    
    return JSONResponse(
        status_code=500,
        content={
            "error": "Internal server error",
            "detail": "An unexpected error occurred. Please try again later.",
            "path": request.url.path
        }
    )


# HTTP exception handler
@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    """Handler for HTTP exceptions"""
    
    logger.warning("HTTP exception",
                  path=request.url.path,
                  method=request.method,
                  status_code=exc.status_code,
                  detail=exc.detail)
    
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": exc.detail,
            "status_code": exc.status_code,
            "path": request.url.path
        }
    )


# Request logging middleware
@app.middleware("http")
async def log_requests(request: Request, call_next):
    """Log all HTTP requests"""
    
    start_time = __import__("time").time()
    
    # Call the next middleware/endpoint
    response = await call_next(request)
    
    # Calculate processing time
    process_time = __import__("time").time() - start_time
    
    # Log request
    logger.info("HTTP request processed",
               method=request.method,
               path=request.url.path,
               status_code=response.status_code,
               process_time_ms=round(process_time * 1000, 2),
               client_ip=request.client.host if request.client else None)
    
    return response


# Include API routes
app.include_router(api_router, prefix="/api/v1")


# Root endpoint
@app.get("/")
async def root():
    """Root endpoint with basic application information"""
    return {
        "app": settings.app_name,
        "version": settings.version,
        "status": "running",
        "docs": "/docs",
        "health": "/api/v1/health"
    }


# Development server startup
if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.debug,
        log_level="debug" if settings.debug else "info"
    )
