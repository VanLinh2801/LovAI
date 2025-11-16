from fastapi import APIRouter
from .health import router as health_router
from .logs import router as logs_router
from .notifications import router as notifications_router
from .admin import router as admin_router

api_router = APIRouter()

api_router.include_router(health_router, prefix="/health", tags=["health"])
api_router.include_router(logs_router, prefix="/logs", tags=["logs"])
api_router.include_router(notifications_router, prefix="/notify", tags=["notifications"])
api_router.include_router(admin_router, prefix="/admin", tags=["admin"])

__all__ = ["api_router"]
