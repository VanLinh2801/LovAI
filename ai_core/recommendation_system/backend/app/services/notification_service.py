import json
from typing import Dict, List, Any, Optional
from datetime import datetime
import firebase_admin
from firebase_admin import credentials, messaging
from sqlalchemy.orm import Session
from sqlalchemy import and_
from ..models.user import User
from ..models.couple import Couple, CoupleMembers
from ..models.recommendation import Recommendation
from ..core.config import settings
from ..core.logging import get_logger

logger = get_logger(__name__)


class NotificationService:
    """Service for sending push notifications via Firebase Cloud Messaging"""
    
    def __init__(self, db: Session):
        self.db = db
        self._initialize_firebase()
    
    def _initialize_firebase(self):
        """Initialize Firebase Admin SDK"""
        try:
            if settings.firebase_credentials_path and not firebase_admin._apps:
                cred = credentials.Certificate(settings.firebase_credentials_path)
                firebase_admin.initialize_app(cred, {
                    'projectId': settings.firebase_project_id,
                })
                logger.info("Firebase initialized successfully")
            elif firebase_admin._apps:
                logger.info("Firebase already initialized")
            else:
                logger.warning("Firebase credentials not configured")
        except Exception as e:
            logger.error("Failed to initialize Firebase", error=str(e))
    
    async def send_recommendation_notification(
        self, 
        recommendation_id: int,
        custom_message: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Send push notification for new recommendations
        
        Args:
            recommendation_id: ID of the recommendation
            custom_message: Optional custom message override
            
        Returns:
            Dictionary with send results
        """
        
        try:
            # Get recommendation data
            recommendation = self.db.query(Recommendation).filter(
                Recommendation.id == recommendation_id
            ).first()
            
            if not recommendation:
                logger.error("Recommendation not found", recommendation_id=recommendation_id)
                return {"success": False, "error": "Recommendation not found"}
            
            # Get couple and users
            couple = self.db.query(Couple).filter(Couple.id == recommendation.couple_id).first()
            if not couple:
                logger.error("Couple not found", couple_id=recommendation.couple_id)
                return {"success": False, "error": "Couple not found"}
            
            # Get users in the couple
            users = self._get_couple_users(recommendation.couple_id)
            if not users:
                logger.warning("No users found for couple", couple_id=recommendation.couple_id)
                return {"success": False, "error": "No users to notify"}
            
            # Extract top recommendation for notification
            top_place = self._get_top_recommendation(recommendation.results_jsonb)
            if not top_place:
                logger.error("No recommendations to send", recommendation_id=recommendation_id)
                return {"success": False, "error": "No recommendations available"}
            
            # Create notification payload
            notification_data = self._create_notification_payload(
                top_place, recommendation, custom_message
            )
            
            # Send to all users in the couple
            send_results = []
            for user in users:
                if user.fcm_token:
                    result = await self._send_to_user(user, notification_data)
                    send_results.append({
                        "user_id": user.id,
                        "success": result["success"],
                        "message_id": result.get("message_id"),
                        "error": result.get("error")
                    })
                else:
                    logger.warning("User has no FCM token", user_id=user.id)
                    send_results.append({
                        "user_id": user.id,
                        "success": False,
                        "error": "No FCM token"
                    })
            
            # Update recommendation as delivered
            recommendation.delivered_at = datetime.utcnow()
            recommendation.notification_sent = True
            self.db.commit()
            
            success_count = sum(1 for result in send_results if result["success"])
            
            logger.info("Recommendation notification sent",
                       recommendation_id=recommendation_id,
                       total_users=len(users),
                       successful_sends=success_count)
            
            return {
                "success": success_count > 0,
                "recommendation_id": recommendation_id,
                "total_users": len(users),
                "successful_sends": success_count,
                "results": send_results
            }
            
        except Exception as e:
            logger.error("Error sending recommendation notification",
                        recommendation_id=recommendation_id,
                        error=str(e))
            return {"success": False, "error": str(e)}
    
    async def _send_to_user(self, user: User, notification_data: Dict[str, Any]) -> Dict[str, Any]:
        """Send notification to a specific user"""
        
        try:
            if not firebase_admin._apps:
                logger.error("Firebase not initialized")
                return {"success": False, "error": "Firebase not initialized"}
            
            # Create FCM message
            message = messaging.Message(
                notification=messaging.Notification(
                    title=notification_data["title"],
                    body=notification_data["body"],
                    image=notification_data.get("image_url")
                ),
                data={
                    "type": "recommendation",
                    "recommendation_id": str(notification_data["recommendation_id"]),
                    "place_id": notification_data.get("place_id", ""),
                    "deeplink": notification_data.get("deeplink", ""),
                    "click_action": notification_data.get("deeplink", "")
                },
                token=user.fcm_token,
                android=messaging.AndroidConfig(
                    notification=messaging.AndroidNotification(
                        icon="ic_notification",
                        color="#FF6B35",
                        click_action=notification_data.get("deeplink", "")
                    ),
                    priority="high"
                ),
                apns=messaging.APNSConfig(
                    payload=messaging.APNSPayload(
                        aps=messaging.Aps(
                            alert=messaging.ApsAlert(
                                title=notification_data["title"],
                                body=notification_data["body"]
                            ),
                            badge=1,
                            sound="default"
                        )
                    )
                )
            )
            
            # Send message
            response = messaging.send(message)
            
            logger.info("Notification sent successfully",
                       user_id=user.id,
                       message_id=response)
            
            return {
                "success": True,
                "message_id": response
            }
            
        except Exception as e:
            logger.error("Error sending notification to user",
                        user_id=user.id,
                        error=str(e))
            return {
                "success": False,
                "error": str(e)
            }
    
    def _get_couple_users(self, couple_id: int) -> List[User]:
        """Get all users in a couple"""
        
        users = self.db.query(User).join(CoupleMembers).filter(
            CoupleMembers.couple_id == couple_id
        ).all()
        
        return users
    
    def _get_top_recommendation(self, results_jsonb: List[Dict[str, Any]]) -> Optional[Dict[str, Any]]:
        """Get the top recommendation from results"""
        
        if not results_jsonb or not isinstance(results_jsonb, list):
            return None
        
        # Sort by score if not already sorted
        sorted_results = sorted(results_jsonb, key=lambda x: x.get("score", 0), reverse=True)
        
        return sorted_results[0] if sorted_results else None
    
    def _create_notification_payload(
        self,
        top_place: Dict[str, Any],
        recommendation: Recommendation,
        custom_message: Optional[str] = None
    ) -> Dict[str, Any]:
        """Create notification payload"""
        
        place_name = top_place.get("name", "a great place")
        place_type = top_place.get("inferred_category", "venue")
        distance = top_place.get("distance_km", 0)
        rating = top_place.get("rating", 0)
        
        # Create title and body
        if custom_message:
            title = "New Date Suggestion 💕"
            body = custom_message
        else:
            title = f"Perfect spot for your date! 💕"
            body = f"We found {place_name}, a {place_type}"
            
            if distance > 0:
                body += f" just {distance:.1f}km away"
            
            if rating and rating > 0:
                body += f" with {rating:.1f}⭐ rating"
            
            body += ". Tap to see on map!"
        
        # Create deeplink to Google Maps
        deeplink = top_place.get("maps_deeplink", "")
        if not deeplink:
            lat = top_place.get("lat")
            lng = top_place.get("lng")
            if lat and lng:
                deeplink = f"https://www.google.com/maps/search/?api=1&query={lat},{lng}"
        
        return {
            "title": title,
            "body": body,
            "recommendation_id": recommendation.id,
            "place_id": top_place.get("place_id", ""),
            "place_name": place_name,
            "deeplink": deeplink,
            "image_url": None  # Could add place photos in future
        }
    
    async def send_test_notification(
        self, 
        user_id: int, 
        message: str = "Test notification from LovAI! 💕"
    ) -> Dict[str, Any]:
        """Send a test notification to a specific user"""
        
        try:
            user = self.db.query(User).filter(User.id == user_id).first()
            if not user:
                return {"success": False, "error": "User not found"}
            
            if not user.fcm_token:
                return {"success": False, "error": "User has no FCM token"}
            
            notification_data = {
                "title": "LovAI Test",
                "body": message,
                "recommendation_id": 0,
                "place_id": "",
                "deeplink": ""
            }
            
            result = await self._send_to_user(user, notification_data)
            
            logger.info("Test notification sent", user_id=user_id, result=result)
            
            return result
            
        except Exception as e:
            logger.error("Error sending test notification", user_id=user_id, error=str(e))
            return {"success": False, "error": str(e)}
    
    def update_user_fcm_token(self, user_id: int, fcm_token: str) -> bool:
        """Update user's FCM token"""
        
        try:
            user = self.db.query(User).filter(User.id == user_id).first()
            if not user:
                logger.error("User not found for FCM token update", user_id=user_id)
                return False
            
            user.fcm_token = fcm_token
            self.db.commit()
            
            logger.info("FCM token updated", user_id=user_id)
            return True
            
        except Exception as e:
            logger.error("Error updating FCM token", user_id=user_id, error=str(e))
            self.db.rollback()
            return False
    
    def get_notification_history(
        self, 
        couple_id: int, 
        limit: int = 10
    ) -> List[Dict[str, Any]]:
        """Get notification history for a couple"""
        
        try:
            recommendations = self.db.query(Recommendation).filter(
                and_(
                    Recommendation.couple_id == couple_id,
                    Recommendation.notification_sent == True
                )
            ).order_by(Recommendation.delivered_at.desc()).limit(limit).all()
            
            history = []
            for rec in recommendations:
                top_place = self._get_top_recommendation(rec.results_jsonb)
                
                history.append({
                    "recommendation_id": rec.id,
                    "delivered_at": rec.delivered_at.isoformat() if rec.delivered_at else None,
                    "place_name": top_place.get("name") if top_place else "Unknown",
                    "place_type": top_place.get("inferred_category") if top_place else "Unknown",
                    "total_recommendations": len(rec.results_jsonb) if rec.results_jsonb else 0
                })
            
            return history
            
        except Exception as e:
            logger.error("Error getting notification history", couple_id=couple_id, error=str(e))
            return []
