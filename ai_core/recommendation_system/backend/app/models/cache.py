from sqlalchemy import Column, String, DateTime, Text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from ..core.database import Base


class PlaceCache(Base):
    __tablename__ = "place_cache"
    
    key = Column(Text, primary_key=True)  # Composite key based on location + filters
    payload_jsonb = Column(JSONB, nullable=False)
    expires_at = Column(DateTime(timezone=True), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class WeatherCache(Base):
    __tablename__ = "weather_cache"
    
    key = Column(Text, primary_key=True)  # Composite key based on location + time
    payload_jsonb = Column(JSONB, nullable=False)
    expires_at = Column(DateTime(timezone=True), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
