from sqlalchemy import Column, Integer, String, DateTime, Float, ForeignKey, Boolean
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from ..core.database import Base


class Recommendation(Base):
    __tablename__ = "recommendations"
    
    id = Column(Integer, primary_key=True, index=True)
    couple_id = Column(Integer, ForeignKey("couples.id"), nullable=False)
    
    # Time window this recommendation was generated for
    trigger_window = Column(JSONB, nullable=False)  # {"start": ts, "end": ts}
    
    # Criteria used for generating this recommendation
    criteria_jsonb = Column(JSONB, nullable=False)  # {lat, lng, types, radius_km, budget_range, weather_ok, algo_version}
    
    # Results from the recommendation algorithm
    results_jsonb = Column(JSONB, nullable=False)  # [{place_id, name, address, rating, distance_km, price_level, source, score}]
    
    # Delivery and engagement tracking
    delivered_at = Column(DateTime(timezone=True), nullable=True)
    notification_sent = Column(Boolean, default=False)
    
    # Engagement signals (inferred from logs)
    engagement_signals_jsonb = Column(JSONB, default=dict)  # {accepted_inferred, arrived_at, dwell_minutes, matched_place_id}
    
    # Metadata
    algorithm_version = Column(String(50), default="1.0")
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    
    # Relationships
    couple = relationship("Couple", back_populates="recommendations") 
