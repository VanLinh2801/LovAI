from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from datetime import datetime
from ..core.database import get_db
from ..core.config import settings
from ..core.scheduler import scheduler

router = APIRouter()


@router.get("/")
async def health_check(db: Session = Depends(get_db)):
    """Basic health check endpoint"""
    
    try:
        # Test database connection
        db.execute("SELECT 1")
        db_status = "healthy"
    except Exception as e:
        db_status = f"error: {str(e)}"
    
    # Check scheduler status
    scheduler_status = scheduler.get_job_status()
    
    return {
        "status": "healthy" if db_status == "healthy" else "degraded",
        "timestamp": datetime.utcnow().isoformat(),
        "version": settings.version,
        "database": db_status,
        "scheduler": {
            "running": scheduler_status["scheduler_running"],
            "jobs_count": len(scheduler_status["jobs"])
        },
        "components": {
            "database": db_status == "healthy",
            "scheduler": scheduler_status["scheduler_running"],
            "external_apis": True  # Would implement actual checks in production
        }
    }


@router.get("/detailed")
async def detailed_health_check(db: Session = Depends(get_db)):
    """Detailed health check with scheduler job information"""
    
    try:
        # Test database connection
        db.execute("SELECT 1")
        db_status = "healthy"
    except Exception as e:
        db_status = f"error: {str(e)}"
    
    # Get detailed scheduler status
    scheduler_status = scheduler.get_job_status()
    
    return {
        "status": "healthy" if db_status == "healthy" else "degraded",
        "timestamp": datetime.utcnow().isoformat(),
        "version": settings.version,
        "app_name": settings.app_name,
        "database": {
            "status": db_status,
            "url": settings.database_url.split("@")[-1] if "@" in settings.database_url else "masked"
        },
        "scheduler": scheduler_status,
        "configuration": {
            "max_recommendations": settings.max_recommendations,
            "default_radius_km": settings.default_radius_km,
            "cache_ttl_seconds": settings.cache_ttl_seconds,
            "scheduler_enabled": settings.scheduler_enabled
        },
        "external_services": {
            "openmeteo": {
                "configured": bool(settings.openmeteo_base_url),
                "base_url": settings.openmeteo_base_url
            },
            "geoapify": {
                "configured": bool(settings.geoapify_api_key),
                "base_url": settings.geoapify_base_url
            },
            "firebase": {
                "configured": bool(settings.firebase_credentials_path)
            }
        }
    }
