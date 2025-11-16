from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.jobstores.sqlalchemy import SQLAlchemyJobStore
from datetime import datetime, timedelta
from typing import List
from sqlalchemy.orm import sessionmaker
from sqlalchemy import and_

from .config import settings
from .database import engine, get_db
from .logging import get_logger
from ..models.couple import Couple
from ..models.log import Log
from ..models.recommendation import Recommendation
from ..services.recommendation_service import RecommendationService
from ..services.notification_service import NotificationService
from ..services.cache_service import CacheService

logger = get_logger(__name__)


class RecommendationScheduler:
    """Scheduler for automated recommendation generation and management"""
    
    def __init__(self):
        self.scheduler = None
        self._setup_scheduler()
    
    def _setup_scheduler(self):
        """Initialize the scheduler with job stores"""
        
        # Configure job store to use the same database
        jobstores = {
            'default': SQLAlchemyJobStore(url=settings.database_url, tablename='scheduler_jobs')
        }
        
        job_defaults = {
            'coalesce': True,
            'max_instances': 1,
            'misfire_grace_time': 300  # 5 minutes
        }
        
        self.scheduler = AsyncIOScheduler(
            jobstores=jobstores,
            job_defaults=job_defaults,
            timezone='UTC'
        )
        
        logger.info("Scheduler configured successfully")
    
    async def start(self):
        """Start the scheduler and add recurring jobs"""
        
        if not settings.scheduler_enabled:
            logger.info("Scheduler disabled in configuration")
            return
        
        try:
            self.scheduler.start()
            
            # Add recurring jobs
            await self._add_recurring_jobs()
            
            logger.info("Scheduler started successfully")
            
        except Exception as e:
            logger.error("Failed to start scheduler", error=str(e))
            raise
    
    async def stop(self):
        """Stop the scheduler"""
        
        if self.scheduler and self.scheduler.running:
            self.scheduler.shutdown(wait=True)
            logger.info("Scheduler stopped")
    
    async def _add_recurring_jobs(self):
        """Add all recurring jobs to the scheduler"""
        
        # Daily preference analysis job
        self.scheduler.add_job(
            func=self.daily_pref_job,
            trigger='cron',
            hour=settings.daily_pref_job_hour,
            minute=0,
            id='daily_pref_analysis',
            replace_existing=True,
            name='Daily Preference Analysis'
        )
        
        # Recommendation generation job (every 2 hours)
        self.scheduler.add_job(
            func=self.generate_reco_job,
            trigger='interval',
            minutes=settings.generate_reco_job_interval_minutes,
            id='generate_recommendations',
            replace_existing=True,
            name='Generate Recommendations'
        )
        
        # Post-event inference job (daily at 11 PM)
        self.scheduler.add_job(
            func=self.post_event_infer_job,
            trigger='cron',
            hour=settings.post_event_infer_job_hour,
            minute=0,
            id='post_event_inference',
            replace_existing=True,
            name='Post-Event Inference'
        )
        
        # Cache cleanup job (daily at 2 AM)
        self.scheduler.add_job(
            func=self.cache_cleanup_job,
            trigger='cron',
            hour=2,
            minute=0,
            id='cache_cleanup',
            replace_existing=True,
            name='Cache Cleanup'
        )
        
        logger.info("Recurring jobs added to scheduler")
    
    async def daily_pref_job(self):
        """
        Daily job to analyze user preferences and identify upcoming time windows
        Runs once or twice per day to analyze logs and predict upcoming preferred time slots
        """
        
        logger.info("Starting daily preference analysis job")
        
        try:
            db = next(get_db())
            recommendation_service = RecommendationService(db)
            
            # Get all active couples
            couples = db.query(Couple).all()
            
            processed_couples = 0
            predicted_windows = 0
            
            for couple in couples:
                try:
                    # Infer upcoming time windows for this couple
                    windows = recommendation_service.infer_time_windows(couple.id)
                    
                    if windows:
                        predicted_windows += len(windows)
                        logger.debug("Predicted time windows for couple",
                                   couple_id=couple.id,
                                   windows_count=len(windows))
                        
                        # Store predictions or trigger recommendation generation
                        # For now, we'll just log them - in production you might want to store these
                        for window in windows:
                            if window.get("confidence", 0) > 0.3:  # Only high-confidence windows
                                logger.info("High confidence window predicted",
                                          couple_id=couple.id,
                                          start_time=window["start"].isoformat(),
                                          confidence=window["confidence"])
                    
                    processed_couples += 1
                    
                except Exception as e:
                    logger.error("Error processing couple in daily pref job",
                               couple_id=couple.id,
                               error=str(e))
                    continue
            
            await recommendation_service.close()
            db.close()
            
            logger.info("Daily preference analysis completed",
                       processed_couples=processed_couples,
                       predicted_windows=predicted_windows)
            
        except Exception as e:
            logger.error("Error in daily preference analysis job", error=str(e))
    
    async def generate_reco_job(self):
        """
        Recommendation generation job that runs every 1-2 hours
        Generates recommendations for upcoming time windows
        """
        
        logger.info("Starting recommendation generation job")
        
        try:
            db = next(get_db())
            recommendation_service = RecommendationService(db)
            notification_service = NotificationService(db)
            
            # Get all active couples
            couples = db.query(Couple).all()
            
            generated_recommendations = 0
            sent_notifications = 0
            
            for couple in couples:
                try:
                    # Get upcoming time windows for this couple
                    windows = recommendation_service.infer_time_windows(couple.id)
                    
                    for window in windows:
                        # Only generate for windows starting in the next 2-24 hours
                        now = datetime.utcnow()
                        time_until_window = (window["start"] - now).total_seconds() / 3600  # hours
                        
                        if 2 <= time_until_window <= 24 and window.get("confidence", 0) > 0.3:
                            # Generate recommendations for this window
                            result = await recommendation_service.generate_recommendations_for_couple(
                                couple.id, 
                                {"start": window["start"], "end": window["end"]},
                                force_regenerate=False
                            )
                            
                            if result:
                                generated_recommendations += 1
                                recommendation_id = result["recommendation_id"]
                                
                                # Send notification
                                notification_result = await notification_service.send_recommendation_notification(
                                    recommendation_id
                                )
                                
                                if notification_result["success"]:
                                    sent_notifications += 1
                                
                                logger.info("Generated and sent recommendation",
                                          couple_id=couple.id,
                                          recommendation_id=recommendation_id,
                                          notification_sent=notification_result["success"])
                
                except Exception as e:
                    logger.error("Error processing couple in generate reco job",
                               couple_id=couple.id,
                               error=str(e))
                    continue
            
            await recommendation_service.close()
            db.close()
            
            logger.info("Recommendation generation job completed",
                       processed_couples=len(couples),
                       generated_recommendations=generated_recommendations,
                       sent_notifications=sent_notifications)
            
        except Exception as e:
            logger.error("Error in recommendation generation job", error=str(e))
    
    async def post_event_infer_job(self):
        """
        Post-event inference job that runs daily at night
        Compares logs with recommendations to infer engagement signals
        """
        
        logger.info("Starting post-event inference job")
        
        try:
            db = next(get_db())
            
            # Get recommendations from the last 7 days that don't have engagement signals
            cutoff_date = datetime.utcnow() - timedelta(days=7)
            
            recommendations = db.query(Recommendation).filter(
                and_(
                    Recommendation.created_at >= cutoff_date,
                    Recommendation.engagement_signals_jsonb == {}
                )
            ).all()
            
            processed_recommendations = 0
            inferred_engagements = 0
            
            for recommendation in recommendations:
                try:
                    # Get logs for this couple in the time window
                    window_start = datetime.fromisoformat(
                        recommendation.trigger_window["start"].replace('Z', '+00:00')
                    )
                    window_end = datetime.fromisoformat(
                        recommendation.trigger_window["end"].replace('Z', '+00:00')
                    )
                    
                    # Extend window by 2 hours on each side for flexibility
                    search_start = window_start - timedelta(hours=2)
                    search_end = window_end + timedelta(hours=2)
                    
                    logs = db.query(Log).filter(
                        and_(
                            Log.couple_id == recommendation.couple_id,
                            Log.started_at >= search_start,
                            Log.started_at <= search_end
                        )
                    ).all()
                    
                    engagement_signals = self._infer_engagement_from_logs(
                        logs, recommendation.results_jsonb, window_start, window_end
                    )
                    
                    if engagement_signals:
                        recommendation.engagement_signals_jsonb = engagement_signals
                        db.commit()
                        inferred_engagements += 1
                        
                        logger.debug("Inferred engagement signals",
                                   recommendation_id=recommendation.id,
                                   signals=engagement_signals)
                    
                    processed_recommendations += 1
                    
                except Exception as e:
                    logger.error("Error processing recommendation in post-event job",
                               recommendation_id=recommendation.id,
                               error=str(e))
                    continue
            
            db.close()
            
            logger.info("Post-event inference job completed",
                       processed_recommendations=processed_recommendations,
                       inferred_engagements=inferred_engagements)
            
        except Exception as e:
            logger.error("Error in post-event inference job", error=str(e))
    
    def _infer_engagement_from_logs(
        self,
        logs: List[Log],
        recommendations: List[dict],
        window_start: datetime,
        window_end: datetime
    ) -> dict:
        """Infer engagement signals from logs and recommendations"""
        
        if not logs or not recommendations:
            return {"accepted_inferred": False}
        
        engagement_signals = {
            "accepted_inferred": False,
            "arrived_at": None,
            "dwell_minutes": None,
            "matched_place_id": None,
            "confidence": 0.0
        }
        
        # Check if any log location matches recommended places
        for log in logs:
            for rec_place in recommendations:
                rec_lat = rec_place.get("lat")
                rec_lng = rec_place.get("lng")
                
                if rec_lat and rec_lng:
                    # Calculate distance between log and recommended place
                    distance = self._calculate_distance(
                        log.lat, log.lng, rec_lat, rec_lng
                    )
                    
                    # If within 100 meters of recommended place
                    if distance <= 0.1:  # 100 meters
                        engagement_signals["accepted_inferred"] = True
                        engagement_signals["arrived_at"] = log.started_at.isoformat()
                        engagement_signals["matched_place_id"] = rec_place.get("place_id", "")
                        
                        # Calculate dwell time if available
                        if log.ended_at:
                            dwell_minutes = (log.ended_at - log.started_at).total_seconds() / 60
                            engagement_signals["dwell_minutes"] = int(dwell_minutes)
                        
                        # Higher confidence for closer matches and longer stays
                        confidence = 0.8  # Base confidence for location match
                        if engagement_signals["dwell_minutes"] and engagement_signals["dwell_minutes"] > 60:
                            confidence = min(1.0, confidence + 0.1)  # Bonus for longer stays
                        
                        engagement_signals["confidence"] = confidence
                        
                        return engagement_signals
        
        return engagement_signals
    
    def _calculate_distance(self, lat1: float, lng1: float, lat2: float, lng2: float) -> float:
        """Calculate distance between two points in km"""
        import math
        
        R = 6371  # Earth's radius in km
        
        dlat = math.radians(lat2 - lat1)
        dlng = math.radians(lng2 - lng1)
        
        a = (math.sin(dlat / 2) * math.sin(dlat / 2) +
             math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) *
             math.sin(dlng / 2) * math.sin(dlng / 2))
        
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        distance = R * c
        
        return distance
    
    async def cache_cleanup_job(self):
        """Clean up expired cache entries"""
        
        logger.info("Starting cache cleanup job")
        
        try:
            db = next(get_db())
            cache_service = CacheService(db)
            
            await cache_service.cleanup_expired_cache()
            
            db.close()
            
            logger.info("Cache cleanup job completed")
            
        except Exception as e:
            logger.error("Error in cache cleanup job", error=str(e))
    
    def get_job_status(self) -> dict:
        """Get status of all scheduled jobs"""
        
        if not self.scheduler:
            return {"scheduler_running": False, "jobs": []}
        
        jobs = []
        for job in self.scheduler.get_jobs():
            jobs.append({
                "id": job.id,
                "name": job.name,
                "next_run": job.next_run_time.isoformat() if job.next_run_time else None,
                "trigger": str(job.trigger)
            })
        
        return {
            "scheduler_running": self.scheduler.running,
            "jobs": jobs
        }
    
    async def trigger_job_manually(self, job_id: str) -> bool:
        """Manually trigger a specific job"""
        
        try:
            if job_id == "daily_pref_analysis":
                await self.daily_pref_job()
            elif job_id == "generate_recommendations":
                await self.generate_reco_job()
            elif job_id == "post_event_inference":
                await self.post_event_infer_job()
            elif job_id == "cache_cleanup":
                await self.cache_cleanup_job()
            else:
                logger.error("Unknown job ID", job_id=job_id)
                return False
            
            logger.info("Manual job execution completed", job_id=job_id)
            return True
            
        except Exception as e:
            logger.error("Error in manual job execution", job_id=job_id, error=str(e))
            return False


# Global scheduler instance
scheduler = RecommendationScheduler()
