from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Dict, Any

from ..core.database import get_db
from ..core.scheduler import scheduler
from ..services.recommendation_service import RecommendationService
from .schemas import (
    ManualJobTrigger, 
    SuccessResponse, 
    RecommendationResponse
)
from ..core.logging import get_logger

logger = get_logger(__name__)

router = APIRouter()


@router.get("/scheduler/status")
async def get_scheduler_status():
    """
    Get current status of the scheduler and all jobs
    
    Returns information about the scheduler state and all configured jobs
    including their next run times and trigger configurations.
    """
    
    try:
        status = scheduler.get_job_status()
        
        logger.info("Retrieved scheduler status",
                   running=status["scheduler_running"],
                   jobs_count=len(status["jobs"]))
        
        return status
    
    except Exception as e:
        logger.error("Error retrieving scheduler status", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve scheduler status"
        )


@router.post("/scheduler/trigger", response_model=SuccessResponse)
async def trigger_scheduler_job(job_data: ManualJobTrigger):
    """
    Manually trigger a specific scheduler job
    
    Available job IDs:
    - daily_pref_analysis: Analyze user preferences and predict time windows
    - generate_recommendations: Generate recommendations for couples
    - post_event_inference: Analyze logs to infer engagement with recommendations
    - cache_cleanup: Clean up expired cache entries
    """
    
    try:
        success = await scheduler.trigger_job_manually(job_data.job_id)
        
        if success:
            logger.info("Job triggered manually", job_id=job_data.job_id)
            
            return SuccessResponse(
                success=True,
                message=f"Job '{job_data.job_id}' executed successfully",
                data={"job_id": job_data.job_id}
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Failed to execute job '{job_data.job_id}'"
            )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error triggering job", job_id=job_data.job_id, error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to trigger job '{job_data.job_id}'"
        )


@router.post("/recommendations/generate/{couple_id}", response_model=RecommendationResponse)
async def generate_recommendations_for_couple(
    couple_id: int,
    force_regenerate: bool = False,
    db: Session = Depends(get_db)
):
    """
    Manually generate recommendations for a specific couple
    
    This endpoint generates recommendations for the next predicted time window
    for the specified couple. Useful for testing and debugging.
    """
    
    try:
        recommendation_service = RecommendationService(db)
        
        # Get next predicted time window
        windows = recommendation_service.infer_time_windows(couple_id)
        
        if not windows:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"No predicted time windows found for couple {couple_id}"
            )
        
        # Use the first predicted window
        next_window = windows[0]
        trigger_window = {
            "start": next_window["start"],
            "end": next_window["end"]
        }
        
        # Generate recommendations
        result = await recommendation_service.generate_recommendations_for_couple(
            couple_id, 
            trigger_window,
            force_regenerate=force_regenerate
        )
        
        await recommendation_service.close()
        
        if result:
            logger.info("Manual recommendation generation completed",
                       couple_id=couple_id,
                       recommendation_id=result["recommendation_id"])
            
            return RecommendationResponse(**result)
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Failed to generate recommendations for couple {couple_id}"
            )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error generating recommendations", 
                    couple_id=couple_id, 
                    error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to generate recommendations"
        )


@router.get("/recommendations/{couple_id}/windows")
async def get_predicted_time_windows(
    couple_id: int,
    db: Session = Depends(get_db)
):
    """
    Get predicted time windows for a couple
    
    Returns the predicted time windows based on the couple's
    historical dating patterns.
    """
    
    try:
        recommendation_service = RecommendationService(db)
        
        windows = recommendation_service.infer_time_windows(couple_id)
        
        await recommendation_service.close()
        
        # Format windows for response
        formatted_windows = []
        for window in windows:
            formatted_windows.append({
                "start": window["start"].isoformat(),
                "end": window["end"].isoformat(),
                "confidence": window.get("confidence", 0.0),
                "duration_hours": (window["end"] - window["start"]).total_seconds() / 3600
            })
        
        logger.info("Retrieved predicted time windows",
                   couple_id=couple_id,
                   windows_count=len(formatted_windows))
        
        return {
            "couple_id": couple_id,
            "predicted_windows": formatted_windows,
            "total_windows": len(formatted_windows)
        }
    
    except Exception as e:
        logger.error("Error retrieving predicted time windows", 
                    couple_id=couple_id, 
                    error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve predicted time windows"
        )


@router.get("/cache/stats")
async def get_cache_statistics(db: Session = Depends(get_db)):
    """
    Get cache statistics and information
    
    Returns information about current cache usage, hit rates,
    and cache entry counts.
    """
    
    try:
        from ..models.cache import PlaceCache, WeatherCache
        from datetime import datetime
        
        now = datetime.utcnow()
        
        # Count cache entries
        place_cache_total = db.query(PlaceCache).count()
        place_cache_expired = db.query(PlaceCache).filter(PlaceCache.expires_at <= now).count()
        place_cache_active = place_cache_total - place_cache_expired
        
        weather_cache_total = db.query(WeatherCache).count()
        weather_cache_expired = db.query(WeatherCache).filter(WeatherCache.expires_at <= now).count()
        weather_cache_active = weather_cache_total - weather_cache_expired
        
        stats = {
            "place_cache": {
                "total_entries": place_cache_total,
                "active_entries": place_cache_active,
                "expired_entries": place_cache_expired
            },
            "weather_cache": {
                "total_entries": weather_cache_total,
                "active_entries": weather_cache_active,
                "expired_entries": weather_cache_expired
            },
            "total_cache_entries": place_cache_total + weather_cache_total,
            "total_active_entries": place_cache_active + weather_cache_active,
            "total_expired_entries": place_cache_expired + weather_cache_expired
        }
        
        logger.info("Retrieved cache statistics", stats=stats)
        
        return stats
    
    except Exception as e:
        logger.error("Error retrieving cache statistics", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve cache statistics"
        )


@router.post("/cache/cleanup", response_model=SuccessResponse)
async def manual_cache_cleanup(db: Session = Depends(get_db)):
    """
    Manually trigger cache cleanup
    
    Removes all expired cache entries from the database.
    """
    
    try:
        from ..services.cache_service import CacheService
        
        cache_service = CacheService(db)
        await cache_service.cleanup_expired_cache()
        
        logger.info("Manual cache cleanup completed")
        
        return SuccessResponse(
            success=True,
            message="Cache cleanup completed successfully"
        )
    
    except Exception as e:
        logger.error("Error during manual cache cleanup", error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to cleanup cache"
        )
