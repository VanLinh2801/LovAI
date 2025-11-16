from sqlalchemy import Column, Integer, String, DateTime, Float, Text, ForeignKey
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from ..core.database import Base


class Log(Base):
    __tablename__ = "logs"
    
    id = Column(Integer, primary_key=True, index=True)
    couple_id = Column(Integer, ForeignKey("couples.id"), nullable=False)
    started_at = Column(DateTime(timezone=True), nullable=False)
    ended_at = Column(DateTime(timezone=True), nullable=True)
    lat = Column(Float, nullable=False)
    lng = Column(Float, nullable=False)
    place_type = Column(String(100), nullable=True)
    place_name = Column(String(255), nullable=True)
    address = Column(Text, nullable=True)
    spent = Column(Float, nullable=True)  # Amount spent
    currency = Column(String(10), default="USD")
    note = Column(Text, nullable=True)
    rating = Column(Float, nullable=True)  # User rating 1-5
    metadata_jsonb = Column(JSONB, default=dict)  # Additional flexible data
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    couple = relationship("Couple", back_populates="logs")
