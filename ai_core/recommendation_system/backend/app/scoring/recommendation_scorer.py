from typing import Dict, List, Any, Tuple
from ..core.config import settings
from .affinity_calculator import AffinityCalculator
from ..core.logging import get_logger

logger = get_logger(__name__)


class RecommendationScorer:
    """Main recommendation scoring engine"""
    
    def __init__(self, affinity_calculator: AffinityCalculator):
        self.affinity_calculator = affinity_calculator
        
        # Scoring weights from config
        self.affinity_weight = settings.affinity_weight
        self.rating_weight = settings.rating_weight
        self.distance_weight = settings.distance_weight
        self.weather_weight = settings.weather_weight
    
    def score_places(
        self,
        places: List[Dict[str, Any]],
        couple_id: int,
        weather_data: Dict[str, Any],
        criteria: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """
        Score and rank places based on the recommendation algorithm
        
        Args:
            places: List of place candidates
            couple_id: Couple ID for personalization
            weather_data: Weather forecast data
            criteria: Additional criteria used for scoring
            
        Returns:
            List of scored and ranked places
        """
        
        scored_places = []
        
        for place in places:
            try:
                score = self.calculate_place_score(place, couple_id, weather_data, criteria)
                
                scored_place = place.copy()
                scored_place.update({
                    "score": score,
                    "score_breakdown": self.get_score_breakdown(place, couple_id, weather_data),
                    "rank": 0  # Will be set after sorting
                })
                
                scored_places.append(scored_place)
                
            except Exception as e:
                logger.error("Error scoring place", 
                           place_id=place.get("place_id"),
                           error=str(e))
                continue
        
        # Sort by score (descending)
        scored_places.sort(key=lambda x: x["score"], reverse=True)
        
        # Assign ranks
        for i, place in enumerate(scored_places):
            place["rank"] = i + 1
        
        logger.info("Places scored and ranked", 
                   total_places=len(places),
                   scored_places=len(scored_places))
        
        return scored_places
    
    def calculate_place_score(
        self,
        place: Dict[str, Any],
        couple_id: int,
        weather_data: Dict[str, Any],
        criteria: Dict[str, Any]
    ) -> float:
        """
        Calculate final score for a place using the scoring formula:
        score = 0.35*affinity + 0.30*rating + 0.20*distance_score + 0.15*weather_bonus
        
        Args:
            place: Place information
            couple_id: Couple ID
            weather_data: Weather forecast
            criteria: Scoring criteria
            
        Returns:
            Final score (0-1)
        """
        
        # 1. Calculate affinity score
        affinity_score = self.affinity_calculator.calculate_place_affinity_score(place, couple_id)
        
        # 2. Get normalized rating score
        rating_score = place.get("normalized_rating", 0.5)
        
        # 3. Get distance score (already calculated in place data)
        distance_score = place.get("distance_score", 0.5)
        
        # 4. Calculate weather bonus
        weather_bonus = self._calculate_weather_bonus(place, weather_data)
        
        # 5. Apply scoring formula
        final_score = (
            self.affinity_weight * affinity_score +
            self.rating_weight * rating_score +
            self.distance_weight * distance_score +
            self.weather_weight * weather_bonus
        )
        
        # Ensure score is between 0 and 1
        final_score = max(0.0, min(1.0, final_score))
        
        return round(final_score, 3)
    
    def get_score_breakdown(
        self,
        place: Dict[str, Any],
        couple_id: int,
        weather_data: Dict[str, Any]
    ) -> Dict[str, float]:
        """Get detailed breakdown of scoring components"""
        
        affinity_score = self.affinity_calculator.calculate_place_affinity_score(place, couple_id)
        rating_score = place.get("normalized_rating", 0.5)
        distance_score = place.get("distance_score", 0.5)
        weather_bonus = self._calculate_weather_bonus(place, weather_data)
        
        return {
            "affinity": round(affinity_score, 3),
            "rating": round(rating_score, 3),
            "distance": round(distance_score, 3),
            "weather": round(weather_bonus, 3),
            "weighted_affinity": round(self.affinity_weight * affinity_score, 3),
            "weighted_rating": round(self.rating_weight * rating_score, 3),
            "weighted_distance": round(self.distance_weight * distance_score, 3),
            "weighted_weather": round(self.weather_weight * weather_bonus, 3)
        }
    
    def _calculate_weather_bonus(
        self, 
        place: Dict[str, Any], 
        weather_data: Dict[str, Any]
    ) -> float:
        """
        Calculate weather bonus based on place type and weather conditions
        
        Args:
            place: Place information
            weather_data: Weather forecast data
            
        Returns:
            Weather bonus score (0-1)
        """
        
        weather_score = weather_data.get("weather_ok", 0.5)
        place_category = place.get("inferred_category", "other")
        
        # Outdoor places are more affected by weather
        outdoor_categories = {"tourism", "park", "outdoor"}
        indoor_categories = {"restaurant", "cafe", "bar", "entertainment", "shopping"}
        
        if place_category in outdoor_categories:
            # Outdoor places: weather has full impact
            if weather_score >= 0.7:
                return 1.0
            elif weather_score >= 0.4:
                return 0.5
            else:
                return 0.0
        
        elif place_category in indoor_categories:
            # Indoor places: less affected by weather, but bad weather can be a bonus
            if weather_score < 0.4:
                return 1.0  # Bad weather = better for indoor activities
            elif weather_score < 0.7:
                return 0.7
            else:
                return 0.5  # Good weather = people might prefer outdoor
        
        else:
            # Mixed or unknown categories: moderate weather impact
            if weather_score >= 0.7:
                return 0.8
            elif weather_score >= 0.4:
                return 0.6
            else:
                return 0.4
    
    def filter_by_constraints(
        self,
        places: List[Dict[str, Any]],
        criteria: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """
        Filter places by hard constraints before scoring
        
        Args:
            places: List of places to filter
            criteria: Filtering criteria
            
        Returns:
            Filtered list of places
        """
        
        filtered_places = places.copy()
        
        # Budget range filter
        budget_range = criteria.get("budget_range")
        if budget_range:
            min_price, max_price = budget_range
            filtered_places = [
                place for place in filtered_places
                if min_price <= place.get("price_level", 2) <= max_price
            ]
        
        # Maximum distance filter
        max_distance_km = criteria.get("max_distance_km", settings.max_radius_km)
        filtered_places = [
            place for place in filtered_places
            if place.get("distance_km", 0) <= max_distance_km
        ]
        
        # Open now filter (simplified for MVP)
        open_now = criteria.get("open_now", True)
        if open_now:
            # For MVP, assume places are open during reasonable hours
            # In production, this would check actual opening hours
            pass
        
        logger.info("Applied constraint filters",
                   original_count=len(places),
                   filtered_count=len(filtered_places),
                   budget_range=budget_range,
                   max_distance_km=max_distance_km)
        
        return filtered_places
    
    def adjust_scores_for_diversity(
        self,
        scored_places: List[Dict[str, Any]],
        diversity_factor: float = 0.1
    ) -> List[Dict[str, Any]]:
        """
        Adjust scores to promote diversity in recommendations
        
        Args:
            scored_places: List of scored places
            diversity_factor: How much to adjust for diversity (0-1)
            
        Returns:
            List with adjusted scores for diversity
        """
        
        if len(scored_places) <= 1:
            return scored_places
        
        adjusted_places = scored_places.copy()
        
        # Track categories we've seen
        category_counts = {}
        
        for place in adjusted_places:
            category = place.get("inferred_category", "other")
            category_counts[category] = category_counts.get(category, 0) + 1
            
            # Apply diversity penalty for repeated categories
            if category_counts[category] > 1:
                diversity_penalty = diversity_factor * (category_counts[category] - 1) * 0.1
                place["score"] = max(0.0, place["score"] - diversity_penalty)
                place["diversity_adjusted"] = True
            else:
                place["diversity_adjusted"] = False
        
        # Re-sort by adjusted scores
        adjusted_places.sort(key=lambda x: x["score"], reverse=True)
        
        # Update ranks
        for i, place in enumerate(adjusted_places):
            place["rank"] = i + 1
        
        return adjusted_places
