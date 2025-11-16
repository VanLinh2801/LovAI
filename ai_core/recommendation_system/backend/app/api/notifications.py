from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from ..core.database import get_db
from ..services.notification_service import NotificationService
from .schemas import (
    NotificationTest, 
    FCMTokenUpdate, 
    SuccessResponse, 
    ErrorResponse,
    NotificationHistoryResponse
)
from ..core.logging import get_logger

logger = get_logger(__name__)

router = APIRouter()


@router.post("/test", response_model=SuccessResponse)
async def send_test_notification(
    test_data: NotificationTest,
    db: Session = Depends(get_db)
):
    """
    Send a test notification to a specific user (development endpoint)
    
    This endpoint is useful for testing the notification system during development.
    It sends a test notification to the specified user's FCM token.
    """
    
    try:
        notification_service = NotificationService(db)
        
        result = await notification_service.send_test_notification(
            test_data.user_id,
            test_data.message
        )
        
        if result["success"]:
            logger.info("Test notification sent successfully",
                       user_id=test_data.user_id,
                       message_id=result.get("message_id"))
            
            return SuccessResponse(
                success=True,
                message="Test notification sent successfully",
                data={
                    "user_id": test_data.user_id,
                    "message_id": result.get("message_id")
                }
            )
        else:
            logger.error("Failed to send test notification",
                        user_id=test_data.user_id,
                        error=result.get("error"))
            
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Failed to send notification: {result.get('error')}"
            )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error sending test notification", 
                    user_id=test_data.user_id, 
                    error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to send test notification"
        )


@router.post("/fcm-token", response_model=SuccessResponse)
async def update_fcm_token(
    token_data: FCMTokenUpdate,
    db: Session = Depends(get_db)
):
    """
    Update a user's FCM token for push notifications
    
    This endpoint should be called by client applications when they
    receive a new FCM token or when the token is refreshed.
    """
    
    try:
        notification_service = NotificationService(db)
        
        success = notification_service.update_user_fcm_token(
            token_data.user_id,
            token_data.fcm_token
        )
        
        if success:
            logger.info("FCM token updated successfully", user_id=token_data.user_id)
            
            return SuccessResponse(
                success=True,
                message="FCM token updated successfully",
                data={"user_id": token_data.user_id}
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"User with ID {token_data.user_id} not found"
            )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error updating FCM token", 
                    user_id=token_data.user_id, 
                    error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to update FCM token"
        )


@router.get("/history/{couple_id}", response_model=List[NotificationHistoryResponse])
async def get_notification_history(
    couple_id: int,
    limit: int = 10,
    db: Session = Depends(get_db)
):
    """
    Get notification history for a couple
    
    Returns a list of previously sent recommendations/notifications
    for the specified couple.
    """
    
    try:
        notification_service = NotificationService(db)
        
        history = notification_service.get_notification_history(couple_id, limit)
        
        logger.info("Retrieved notification history",
                   couple_id=couple_id,
                   count=len(history))
        
        return [NotificationHistoryResponse(**item) for item in history]
    
    except Exception as e:
        logger.error("Error retrieving notification history", 
                    couple_id=couple_id, 
                    error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve notification history"
        )


@router.post("/recommendation/{recommendation_id}", response_model=SuccessResponse)
async def send_recommendation_notification(
    recommendation_id: int,
    db: Session = Depends(get_db)
):
    """
    Manually send notification for a specific recommendation
    
    This endpoint can be used to manually trigger notifications
    for existing recommendations. Useful for testing or resending
    failed notifications.
    """
    
    try:
        notification_service = NotificationService(db)
        
        result = await notification_service.send_recommendation_notification(
            recommendation_id
        )
        
        if result["success"]:
            logger.info("Recommendation notification sent",
                       recommendation_id=recommendation_id,
                       successful_sends=result["successful_sends"])
            
            return SuccessResponse(
                success=True,
                message="Recommendation notification sent successfully",
                data={
                    "recommendation_id": recommendation_id,
                    "total_users": result["total_users"],
                    "successful_sends": result["successful_sends"],
                    "results": result["results"]
                }
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Failed to send notification: {result.get('error')}"
            )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error sending recommendation notification", 
                    recommendation_id=recommendation_id, 
                    error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to send recommendation notification"
        )


@router.delete("/fcm-token/{user_id}", response_model=SuccessResponse)
async def remove_fcm_token(
    user_id: int,
    db: Session = Depends(get_db)
):
    """
    Remove a user's FCM token (e.g., when they log out)
    
    This endpoint should be called when a user logs out or
    wants to stop receiving push notifications.
    """
    
    try:
        notification_service = NotificationService(db)
        
        success = notification_service.update_user_fcm_token(user_id, None)
        
        if success:
            logger.info("FCM token removed successfully", user_id=user_id)
            
            return SuccessResponse(
                success=True,
                message="FCM token removed successfully",
                data={"user_id": user_id}
            )
        else:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"User with ID {user_id} not found"
            )
    
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error removing FCM token", user_id=user_id, error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to remove FCM token"
        )
