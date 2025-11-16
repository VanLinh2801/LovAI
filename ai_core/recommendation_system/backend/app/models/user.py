from sqlalchemy import Column, Integer, String, DateTime, Text
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from ..core.database import Base


class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, nullable=True, index=True)
    gender = Column(String(50), nullable=True)
    age = Column(Integer, nullable=True)
    preferences_jsonb = Column(JSONB, default=dict)
    firebase_uid = Column(String(255), unique=True, nullable=True, index=True)
    fcm_token = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    
    # Relationships
    couple_memberships = relationship("CoupleMembers", back_populates="user")
