from datetime import datetime, timedelta
from typing import Dict, List, Any
from collections import Counter
from sqlalchemy.orm import Session
from sqlalchemy import and_
from ..models.log import Log
from ..core.logging import get_logger

logger = get_logger(__name__)


class AffinityCalculator:
    """Calculate user affinity scores for place types and categories"""
    
    def __init__(self, db: Session):
        self.db = db
    
    def calculate_place_type_affinity(
        self, 
        couple_id: int, 
        lookback_days: int = 90
    ) -> Dict[str, float]:
        """
        Calculate affinity scores for different place types based on historical logs
        
        Args:
            couple_id: Couple ID
            lookback_days: Number of days to look back for historical data
            
        Returns:
            Dictionary mapping place types to affinity scores (0-1)
        """
        
        # Get historical logs
        cutoff_date = datetime.utcnow() - timedelta(days=lookback_days)
        
        logs = self.db.query(Log).filter(
            and_(
                Log.couple_id == couple_id,
                Log.created_at >= cutoff_date,
                Log.place_type.isnot(None)
            )
        ).all()
        
        if not logs:
            logger.info("No historical logs found for affinity calculation", 
                       couple_id=couple_id)
            return self._get_default_affinities()
        
        # Count place type frequencies
        place_type_counts = Counter()
        total_visits = 0
        
        for log in logs:
            place_type = log.place_type
            if place_type:
                # Normalize place type
                normalized_type = self._normalize_place_type(place_type)
                place_type_counts[normalized_type] += 1
                total_visits += 1
        
        # Calculate affinity scores
        affinities = {}
        for place_type, count in place_type_counts.items():
            # Base frequency score
            frequency_score = count / total_visits
            
            # Apply recency weighting (more recent visits weighted higher)
            recency_score = self._calculate_recency_weight(logs, place_type)
            
            # Apply rating weighting (higher rated places weighted higher)
            rating_score = self._calculate_rating_weight(logs, place_type)
            
            # Combined affinity score
            affinity = (0.5 * frequency_score + 0.3 * recency_score + 0.2 * rating_score)
            affinities[place_type] = min(affinity, 1.0)
        
        # Ensure all major place types have scores
        default_affinities = self._get_default_affinities()
        for place_type in default_affinities:
            if place_type not in affinities:
                affinities[place_type] = default_affinities[place_type]
        
        logger.info("Calculated place type affinities", 
                   couple_id=couple_id, 
                   total_logs=len(logs),
                   affinities=affinities)
        
        return affinities
    
    def calculate_cuisine_affinity(
        self, 
        couple_id: int, 
        lookback_days: int = 90
    ) -> Dict[str, float]:
        """
        Calculate affinity scores for different cuisines based on historical logs
        
        Args:
            couple_id: Couple ID
            lookback_days: Number of days to look back for historical data
            
        Returns:
            Dictionary mapping cuisines to affinity scores (0-1)
        """
        
        # Get historical logs for restaurants/cafes
        cutoff_date = datetime.utcnow() - timedelta(days=lookback_days)
        
        logs = self.db.query(Log).filter(
            and_(
                Log.couple_id == couple_id,
                Log.created_at >= cutoff_date,
                Log.place_type.in_(["restaurant", "cafe", "food"]),
                Log.note.isnot(None)  # Assuming cuisine info is in notes
            )
        ).all()
        
        if not logs:
            return self._get_default_cuisine_affinities()
        
        # Extract cuisine information from notes/metadata
        cuisine_counts = Counter()
        total_cuisine_visits = 0
        
        for log in logs:
            cuisines = self._extract_cuisines_from_log(log)
            for cuisine in cuisines:
                cuisine_counts[cuisine] += 1
                total_cuisine_visits += 1
        
        if total_cuisine_visits == 0:
            return self._get_default_cuisine_affinities()
        
        # Calculate cuisine affinity scores
        cuisine_affinities = {}
        for cuisine, count in cuisine_counts.items():
            frequency_score = count / total_cuisine_visits
            
            # Apply rating weight for cuisine
            rating_score = self._calculate_cuisine_rating_weight(logs, cuisine)
            
            affinity = 0.7 * frequency_score + 0.3 * rating_score
            cuisine_affinities[cuisine] = min(affinity, 1.0)
        
        logger.info("Calculated cuisine affinities", 
                   couple_id=couple_id,
                   cuisine_affinities=cuisine_affinities)
        
        return cuisine_affinities
    
    def calculate_place_affinity_score(
        self, 
        place: Dict[str, Any], 
        couple_id: int
    ) -> float:
        """
        Calculate affinity score for a specific place
        
        Args:
            place: Place information dictionary
            couple_id: Couple ID
            
        Returns:
            Affinity score (0-1)
        """
        
        # Get place type and category affinities
        place_type_affinities = self.calculate_place_type_affinity(couple_id)
        cuisine_affinities = self.calculate_cuisine_affinity(couple_id)
        
        # Get place category
        place_category = place.get("inferred_category", "other")
        place_type_affinity = place_type_affinities.get(place_category, 0.3)
        
        # Try to infer cuisine from place data
        place_cuisine = self._infer_cuisine_from_place(place)
        cuisine_affinity = cuisine_affinities.get(place_cuisine, 0.3) if place_cuisine else 0.3
        
        # Combine affinities (weighted toward place type)
        combined_affinity = 0.7 * place_type_affinity + 0.3 * cuisine_affinity
        
        return min(combined_affinity, 1.0)
    
    def _normalize_place_type(self, place_type: str) -> str:
        """Normalize place type to standard categories"""
        place_type = place_type.lower().strip()
        
        if any(word in place_type for word in ["restaurant", "dining", "food"]):
            return "restaurant"
        elif any(word in place_type for word in ["cafe", "coffee", "bakery"]):
            return "cafe"
        elif any(word in place_type for word in ["bar", "pub", "brewery", "cocktail"]):
            return "bar"
        elif any(word in place_type for word in ["cinema", "movie", "theater", "entertainment"]):
            return "entertainment"
        elif any(word in place_type for word in ["park", "museum", "attraction", "tourism"]):
            return "tourism"
        else:
            return "other"
    
    def _calculate_recency_weight(self, logs: List[Log], place_type: str) -> float:
        """Calculate recency weight for a place type"""
        
        recent_logs = [
            log for log in logs 
            if self._normalize_place_type(log.place_type) == place_type
        ]
        
        if not recent_logs:
            return 0.0
        
        # Calculate average days since visits
        now = datetime.utcnow()
        total_weight = 0.0
        
        for log in recent_logs:
            days_ago = (now - log.created_at).days
            # Exponential decay: more recent visits weighted higher
            weight = 1.0 / (1.0 + days_ago / 30.0)  # 30-day decay factor
            total_weight += weight
        
        return total_weight / len(recent_logs)
    
    def _calculate_rating_weight(self, logs: List[Log], place_type: str) -> float:
        """Calculate rating weight for a place type"""
        
        rated_logs = [
            log for log in logs 
            if (self._normalize_place_type(log.place_type) == place_type 
                and log.rating is not None)
        ]
        
        if not rated_logs:
            return 0.5  # Neutral weight
        
        # Average rating normalized to 0-1
        avg_rating = sum(log.rating for log in rated_logs) / len(rated_logs)
        return min(avg_rating / 5.0, 1.0)
    
    def _calculate_cuisine_rating_weight(self, logs: List[Log], cuisine: str) -> float:
        """Calculate rating weight for a specific cuisine"""
        
        cuisine_logs = [
            log for log in logs 
            if (cuisine in self._extract_cuisines_from_log(log) 
                and log.rating is not None)
        ]
        
        if not cuisine_logs:
            return 0.5
        
        avg_rating = sum(log.rating for log in cuisine_logs) / len(cuisine_logs)
        return min(avg_rating / 5.0, 1.0)
    
    def _extract_cuisines_from_log(self, log: Log) -> List[str]:
        """Extract cuisine types from log data"""
        
        cuisines = []
        
        # Check notes for cuisine mentions
        if log.note:
            note_lower = log.note.lower()
            
            # Simple keyword matching for cuisines
            cuisine_keywords = {
                "italian": ["italian", "pizza", "pasta", "italian"],
                "chinese": ["chinese", "asian", "dim sum"],
                "japanese": ["japanese", "sushi", "ramen", "japanese"],
                "indian": ["indian", "curry", "indian"],
                "mexican": ["mexican", "tacos", "mexican"],
                "thai": ["thai", "pad thai", "thai"],
                "french": ["french", "bistro", "french"],
                "american": ["american", "burger", "bbq", "american"],
                "mediterranean": ["mediterranean", "greek", "mediterranean"],
                "korean": ["korean", "kimchi", "korean"]
            }
            
            for cuisine, keywords in cuisine_keywords.items():
                if any(keyword in note_lower for keyword in keywords):
                    cuisines.append(cuisine)
        
        # Check metadata for cuisine information
        if log.metadata_jsonb and isinstance(log.metadata_jsonb, dict):
            metadata_cuisine = log.metadata_jsonb.get("cuisine")
            if metadata_cuisine:
                cuisines.append(metadata_cuisine.lower())
        
        return list(set(cuisines))  # Remove duplicates
    
    def _infer_cuisine_from_place(self, place: Dict[str, Any]) -> str:
        """Infer cuisine type from place information"""
        
        place_name = place.get("name", "").lower()
        categories = place.get("categories", [])
        
        # Simple cuisine inference from name and categories
        if any(word in place_name for word in ["pizza", "italian"]):
            return "italian"
        elif any(word in place_name for word in ["sushi", "ramen", "japanese"]):
            return "japanese"
        elif any(word in place_name for word in ["chinese", "dim sum"]):
            return "chinese"
        elif any(word in place_name for word in ["thai", "pad thai"]):
            return "thai"
        elif any(word in place_name for word in ["mexican", "tacos"]):
            return "mexican"
        elif any(word in place_name for word in ["indian", "curry"]):
            return "indian"
        
        # Check categories
        for category in categories:
            if "cuisine" in category.lower():
                return category.lower().split(".")[-1]  # Extract cuisine from category
        
        return None
    
    def _get_default_affinities(self) -> Dict[str, float]:
        """Get default affinity scores for new users"""
        return {
            "restaurant": 0.4,
            "cafe": 0.3,
            "bar": 0.2,
            "entertainment": 0.2,
            "tourism": 0.2,
            "other": 0.1
        }
    
    def _get_default_cuisine_affinities(self) -> Dict[str, float]:
        """Get default cuisine affinity scores"""
        return {
            "italian": 0.3,
            "american": 0.3,
            "chinese": 0.2,
            "japanese": 0.2,
            "mexican": 0.2,
            "thai": 0.2,
            "indian": 0.2,
            "french": 0.2
        }
