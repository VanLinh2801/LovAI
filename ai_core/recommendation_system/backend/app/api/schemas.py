from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
from datetime import datetime


class LogCreate(BaseModel):
    """Schema for creating a new log entry"""
    couple_id: int
    started_at: datetime
    ended_at: Optional[datetime] = None
    lat: float = Field(..., ge=-90, le=90)
    lng: float = Field(..., ge=-180, le=180)
    place_type: Optional[str] = None
    place_name: Optional[str] = None
    address: Optional[str] = None
    spent: Optional[float] = Field(None, ge=0)
    currency: str = "USD"
    note: Optional[str] = None
    rating: Optional[float] = Field(None, ge=1, le=5)
    metadata: Optional[Dict[str, Any]] = None


class LogResponse(BaseModel):
    """Schema for log response"""
    id: int
    couple_id: int
    started_at: datetime
    ended_at: Optional[datetime]
    lat: float
    lng: float
    place_type: Optional[str]
    place_name: Optional[str]
    address: Optional[str]
    spent: Optional[float]
    currency: str
    note: Optional[str]
    rating: Optional[float]
    metadata_jsonb: Optional[Dict[str, Any]]
    created_at: datetime
    
    class Config:
        from_attributes = True


class LocationPing(BaseModel):
    """Schema for location ping data"""
    couple_id: int
    lat: float = Field(..., ge=-90, le=90)
    lng: float = Field(..., ge=-180, le=180)
    timestamp: datetime
    accuracy: Optional[float] = None
    activity_type: Optional[str] = None  # walking, driving, stationary


class NotificationTest(BaseModel):
    """Schema for test notification"""
    user_id: int
    message: Optional[str] = "Test notification from LovAI! 💕"


class FCMTokenUpdate(BaseModel):
    """Schema for FCM token update"""
    user_id: int
    fcm_token: str


class ManualJobTrigger(BaseModel):
    """Schema for manually triggering scheduler jobs"""
    job_id: str = Field(..., description="Job ID to trigger (daily_pref_analysis, generate_recommendations, post_event_inference, cache_cleanup)")


class RecommendationResponse(BaseModel):
    """Schema for recommendation response"""
    recommendation_id: int
    recommendations: List[Dict[str, Any]]
    criteria: Dict[str, Any]
    weather: Optional[Dict[str, Any]]
    generated_at: str


class NotificationHistoryResponse(BaseModel):
    """Schema for notification history response"""
    recommendation_id: int
    delivered_at: Optional[str]
    place_name: str
    place_type: str
    total_recommendations: int


class ErrorResponse(BaseModel):
    """Schema for error responses"""
    error: str
    detail: Optional[str] = None
    timestamp: str


class SuccessResponse(BaseModel):
    """Schema for success responses"""
    success: bool
    message: str
    data: Optional[Dict[str, Any]] = None
