from datetime import datetime, timedelta
from typing import Dict, List, Any, Optional, Tuple
from sqlalchemy.orm import Session
from sqlalchemy import and_, or_, desc
from ..models.couple import Couple
from ..models.log import Log
from ..models.recommendation import Recommendation
from ..services.weather_service import WeatherService
from ..services.place_service import PlaceService
from ..scoring.affinity_calculator import AffinityCalculator
from ..scoring.recommendation_scorer import RecommendationScorer
from ..core.config import settings
from ..core.logging import get_logger

logger = get_logger(__name__)


class RecommendationService:
    """Main service for generating and managing recommendations"""
    
    def __init__(self, db: Session):
        self.db = db
        self.weather_service = WeatherService(db)
        self.place_service = PlaceService(db)
        self.affinity_calculator = AffinityCalculator(db)
        self.scorer = RecommendationScorer(self.affinity_calculator)
    
    async def generate_recommendations_for_couple(
        self,
        couple_id: int,
        trigger_window: Dict[str, datetime],
        force_regenerate: bool = False
    ) -> Optional[Dict[str, Any]]:
        """
        Generate recommendations for a couple for a specific time window
        
        Args:
            couple_id: Couple ID
            trigger_window: Dictionary with 'start' and 'end' datetime
            force_regenerate: Whether to regenerate if recommendations already exist
            
        Returns:
            Generated recommendation data or None if failed
        """
        
        try:
            # Check if recommendations already exist for this window
            if not force_regenerate:
                existing = self._check_existing_recommendations(couple_id, trigger_window)
                if existing:
                    logger.info("Recommendations already exist for window", 
                               couple_id=couple_id,
                               window=trigger_window)
                    return existing
            
            # Get couple data
            couple = self.db.query(Couple).filter(Couple.id == couple_id).first()
            if not couple:
                logger.error("Couple not found", couple_id=couple_id)
                return None
            
            # Determine search criteria
            criteria = await self._build_search_criteria(couple, trigger_window)
            
            # Get weather forecast
            weather_data = await self.weather_service.get_weather_forecast(
                criteria["lat"], 
                criteria["lng"],
                trigger_window["start"],
                trigger_window["end"]
            )
            
            # Search for places
            places = await self.place_service.search_places(
                lat=criteria["lat"],
                lng=criteria["lng"],
                radius_km=criteria["radius_km"],
                place_types=criteria["types"],
                budget_range=criteria.get("budget_range"),
                open_now=criteria.get("open_now", True),
                limit=settings.max_recommendations * 3  # Get more for filtering
            )
            
            if not places:
                logger.warning("No places found for criteria", 
                             couple_id=couple_id, criteria=criteria)
                return None
            
            # Filter by constraints
            filtered_places = self.scorer.filter_by_constraints(places, criteria)
            
            if not filtered_places:
                logger.warning("No places after filtering", 
                             couple_id=couple_id, criteria=criteria)
                return None
            
            # Score and rank places
            scored_places = self.scorer.score_places(
                filtered_places, couple_id, weather_data, criteria
            )
            
            # Apply diversity adjustments
            final_places = self.scorer.adjust_scores_for_diversity(scored_places)
            
            # Select top N recommendations
            top_recommendations = final_places[:settings.max_recommendations]
            
            # Save recommendation record
            recommendation_data = {
                "couple_id": couple_id,
                "trigger_window": {
                    "start": trigger_window["start"].isoformat(),
                    "end": trigger_window["end"].isoformat()
                },
                "criteria_jsonb": criteria,
                "results_jsonb": top_recommendations,
                "algorithm_version": "1.0"
            }
            
            recommendation = Recommendation(**recommendation_data)
            self.db.add(recommendation)
            self.db.commit()
            self.db.refresh(recommendation)
            
            logger.info("Recommendations generated successfully",
                       couple_id=couple_id,
                       recommendation_id=recommendation.id,
                       count=len(top_recommendations))
            
            return {
                "recommendation_id": recommendation.id,
                "recommendations": top_recommendations,
                "criteria": criteria,
                "weather": weather_data,
                "generated_at": datetime.utcnow().isoformat()
            }
            
        except Exception as e:
            logger.error("Error generating recommendations", 
                        couple_id=couple_id, error=str(e))
            return None
    
    async def _build_search_criteria(
        self, 
        couple: Couple, 
        trigger_window: Dict[str, datetime]
    ) -> Dict[str, Any]:
        """Build search criteria based on couple preferences and history"""
        
        # Get default location from recent logs
        default_location = await self._get_default_location(couple.id)
        
        # Get preferred place types from couple preferences and history
        preferred_types = self._get_preferred_place_types(couple)
        
        # Get budget preferences
        budget_range = self._get_budget_preferences(couple)
        
        # Calculate optimal radius based on couple's travel patterns
        optimal_radius = await self._calculate_optimal_radius(couple.id)
        
        criteria = {
            "lat": default_location["lat"],
            "lng": default_location["lng"],
            "types": preferred_types,
            "radius_km": optimal_radius,
            "budget_range": budget_range,
            "open_now": True,
            "time_window": trigger_window,
            "weather_important": True
        }
        
        logger.debug("Built search criteria", couple_id=couple.id, criteria=criteria)
        
        return criteria
    
    async def _get_default_location(self, couple_id: int) -> Dict[str, float]:
        """Get default search location for a couple"""
        
        # Get recent logs to determine typical location
        recent_logs = self.db.query(Log).filter(
            and_(
                Log.couple_id == couple_id,
                Log.created_at >= datetime.utcnow() - timedelta(days=30)
            )
        ).order_by(desc(Log.created_at)).limit(10).all()
        
        if recent_logs:
            # Calculate centroid of recent locations
            avg_lat = sum(log.lat for log in recent_logs) / len(recent_logs)
            avg_lng = sum(log.lng for log in recent_logs) / len(recent_logs)
            
            return {"lat": avg_lat, "lng": avg_lng}
        
        # Default to a major city center (this should be configurable)
        return {"lat": 37.7749, "lng": -122.4194}  # San Francisco
    
    def _get_preferred_place_types(self, couple: Couple) -> List[str]:
        """Get preferred place types for a couple"""
        
        # Check couple preferences
        preferences = couple.preferences_jsonb or {}
        preferred_types = preferences.get("preferred_place_types", [])
        
        if preferred_types:
            return preferred_types
        
        # Use place type affinity calculator
        affinities = self.affinity_calculator.calculate_place_type_affinity(couple.id)
        
        # Get top 3-4 place types by affinity
        sorted_affinities = sorted(affinities.items(), key=lambda x: x[1], reverse=True)
        top_types = [place_type for place_type, score in sorted_affinities[:4] if score > 0.2]
        
        return top_types or ["restaurant", "cafe", "entertainment"]
    
    def _get_budget_preferences(self, couple: Couple) -> Optional[Tuple[int, int]]:
        """Get budget preferences for a couple"""
        
        preferences = couple.preferences_jsonb or {}
        budget_range = preferences.get("budget_range")
        
        if budget_range and len(budget_range) == 2:
            return tuple(budget_range)
        
        # Analyze spending patterns from logs
        recent_logs = self.db.query(Log).filter(
            and_(
                Log.couple_id == couple.id,
                Log.spent.isnot(None),
                Log.created_at >= datetime.utcnow() - timedelta(days=90)
            )
        ).all()
        
        if recent_logs:
            spending_amounts = [log.spent for log in recent_logs]
            avg_spending = sum(spending_amounts) / len(spending_amounts)
            
            # Map spending to price levels (simplified)
            if avg_spending < 30:
                return (1, 2)  # Budget to moderate
            elif avg_spending < 100:
                return (2, 3)  # Moderate to expensive
            else:
                return (3, 4)  # Expensive to very expensive
        
        return None  # No budget constraint
    
    async def _calculate_optimal_radius(self, couple_id: int) -> float:
        """Calculate optimal search radius based on couple's travel patterns"""
        
        recent_logs = self.db.query(Log).filter(
            and_(
                Log.couple_id == couple_id,
                Log.created_at >= datetime.utcnow() - timedelta(days=60)
            )
        ).all()
        
        if len(recent_logs) < 2:
            return settings.default_radius_km
        
        # Calculate distances between consecutive visits
        distances = []
        for i in range(1, len(recent_logs)):
            prev_log = recent_logs[i-1]
            curr_log = recent_logs[i]
            
            distance = self._calculate_distance(
                prev_log.lat, prev_log.lng,
                curr_log.lat, curr_log.lng
            )
            distances.append(distance)
        
        if distances:
            # Use 75th percentile of distances as optimal radius
            distances.sort()
            percentile_75 = distances[int(len(distances) * 0.75)]
            optimal_radius = min(max(percentile_75, 2.0), settings.max_radius_km)
            
            logger.debug("Calculated optimal radius", 
                        couple_id=couple_id, 
                        optimal_radius=optimal_radius,
                        sample_distances=distances[:5])
            
            return optimal_radius
        
        return settings.default_radius_km
    
    def _calculate_distance(self, lat1: float, lng1: float, lat2: float, lng2: float) -> float:
        """Calculate distance between two points using Haversine formula"""
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
    
    def _check_existing_recommendations(
        self, 
        couple_id: int, 
        trigger_window: Dict[str, datetime]
    ) -> Optional[Dict[str, Any]]:
        """Check if recommendations already exist for this time window"""
        
        existing = self.db.query(Recommendation).filter(
            and_(
                Recommendation.couple_id == couple_id,
                Recommendation.trigger_window.contains({
                    "start": trigger_window["start"].isoformat(),
                    "end": trigger_window["end"].isoformat()
                })
            )
        ).first()
        
        if existing:
            return {
                "recommendation_id": existing.id,
                "recommendations": existing.results_jsonb,
                "criteria": existing.criteria_jsonb,
                "generated_at": existing.created_at.isoformat()
            }
        
        return None
    
    def infer_time_windows(self, couple_id: int) -> List[Dict[str, datetime]]:
        """
        Infer upcoming preferred time windows based on historical patterns
        
        Args:
            couple_id: Couple ID
            
        Returns:
            List of predicted time windows for the next few days
        """
        
        # Get historical logs
        logs = self.db.query(Log).filter(
            and_(
                Log.couple_id == couple_id,
                Log.created_at >= datetime.utcnow() - timedelta(days=90)
            )
        ).all()
        
        if not logs:
            # Default windows for new couples
            return self._get_default_time_windows()
        
        # Analyze patterns by day of week and time of day
        time_patterns = self._analyze_time_patterns(logs)
        
        # Generate windows for next 7 days
        upcoming_windows = []
        now = datetime.utcnow()
        
        for days_ahead in range(1, 8):  # Next 7 days
            future_date = now + timedelta(days=days_ahead)
            day_of_week = future_date.weekday()  # 0=Monday, 6=Sunday
            
            # Check if this day/time combination is likely based on patterns
            likely_windows = self._predict_windows_for_day(time_patterns, day_of_week, future_date)
            upcoming_windows.extend(likely_windows)
        
        logger.info("Inferred time windows", 
                   couple_id=couple_id, 
                   windows_count=len(upcoming_windows))
        
        return upcoming_windows
    
    def _analyze_time_patterns(self, logs: List[Log]) -> Dict[str, Any]:
        """Analyze temporal patterns in couple's dating history"""
        
        patterns = {
            "day_preferences": {},  # Day of week preferences
            "time_preferences": {},  # Time of day preferences
            "duration_average": 3.0,  # Default 3 hour duration
        }
        
        for log in logs:
            day_of_week = log.started_at.weekday()
            hour_of_day = log.started_at.hour
            
            # Count day preferences
            patterns["day_preferences"][day_of_week] = patterns["day_preferences"].get(day_of_week, 0) + 1
            
            # Count time preferences (group into 4-hour blocks)
            time_block = hour_of_day // 4  # 0=0-3, 1=4-7, 2=8-11, 3=12-15, 4=16-19, 5=20-23
            patterns["time_preferences"][time_block] = patterns["time_preferences"].get(time_block, 0) + 1
            
            # Calculate average duration if available
            if log.ended_at:
                duration = (log.ended_at - log.started_at).total_seconds() / 3600  # hours
                if 0.5 <= duration <= 8:  # Reasonable duration range
                    patterns["duration_average"] = (patterns.get("duration_average", 3.0) + duration) / 2
        
        return patterns
    
    def _predict_windows_for_day(
        self, 
        patterns: Dict[str, Any], 
        day_of_week: int, 
        target_date: datetime
    ) -> List[Dict[str, datetime]]:
        """Predict likely time windows for a specific day"""
        
        windows = []
        
        # Check if this day of week is preferred
        day_frequency = patterns["day_preferences"].get(day_of_week, 0)
        total_days = sum(patterns["day_preferences"].values())
        
        if total_days == 0:
            return []
        
        day_probability = day_frequency / total_days
        
        # Only suggest windows for days with reasonable probability
        if day_probability < 0.1:  # Less than 10% of historical activity
            return []
        
        # Find preferred time blocks for this day
        time_preferences = patterns["time_preferences"]
        duration = patterns.get("duration_average", 3.0)
        
        for time_block, frequency in time_preferences.items():
            if frequency > 0:
                # Convert time block to hour range
                start_hour = time_block * 4
                
                # Create time window
                start_time = target_date.replace(
                    hour=start_hour, 
                    minute=0, 
                    second=0, 
                    microsecond=0
                )
                end_time = start_time + timedelta(hours=duration)
                
                # Only suggest future windows
                if start_time > datetime.utcnow():
                    windows.append({
                        "start": start_time,
                        "end": end_time,
                        "confidence": day_probability * (frequency / sum(time_preferences.values()))
                    })
        
        return windows
    
    def _get_default_time_windows(self) -> List[Dict[str, datetime]]:
        """Get default time windows for new couples"""
        
        windows = []
        now = datetime.utcnow()
        
        # Default patterns: Weekend evenings and some weekday evenings
        for days_ahead in [1, 2, 5, 6]:  # Tomorrow, day after, Friday, Saturday
            target_date = now + timedelta(days=days_ahead)
            
            # Evening window (7-10 PM)
            start_time = target_date.replace(hour=19, minute=0, second=0, microsecond=0)
            end_time = start_time + timedelta(hours=3)
            
            if start_time > now:
                windows.append({
                    "start": start_time,
                    "end": end_time,
                    "confidence": 0.5
                })
        
        return windows
    
    async def close(self):
        """Close all service connections"""
        await self.weather_service.close()
        await self.place_service.close()
