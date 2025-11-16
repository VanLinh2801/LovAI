from sqlalchemy import Column, Integer, String, DateTime, Date, ForeignKey
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from ..core.database import Base


class Couple(Base):
    __tablename__ = "couples"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=True)
    anniversary_date = Column(Date, nullable=True)
    preferences_jsonb = Column(JSONB, default=dict)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
    
    # Relationships
    members = relationship("CoupleMembers", back_populates="couple")
    logs = relationship("Log", back_populates="couple")
    recommendations = relationship("Recommendation", back_populates="couple")


class CoupleMembers(Base):
    __tablename__ = "couple_members"
    
    id = Column(Integer, primary_key=True, index=True)
    couple_id = Column(Integer, ForeignKey("couples.id"), nullable=False)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    role = Column(String(50), default="member")  # member, admin
    joined_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    couple = relationship("Couple", back_populates="members")
    user = relationship("User", back_populates="couple_memberships")
