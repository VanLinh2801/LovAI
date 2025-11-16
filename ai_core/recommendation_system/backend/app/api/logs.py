from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime

from ..core.database import get_db
from ..models.log import Log
from ..models.couple import Couple
from .schemas import LogCreate, LogResponse, LocationPing, SuccessResponse, ErrorResponse
from ..core.logging import get_logger

logger = get_logger(__name__)

router = APIRouter()


@router.post("/", response_model=LogResponse, status_code=status.HTTP_201_CREATED)
async def create_log(
    log_data: LogCreate,
    db: Session = Depends(get_db)
):
    """
    Create a new log entry after a dating activity
    
    This endpoint is used by clients to record dating activities including:
    - Location and time information
    - Place type and details
    - Spending and rating information
    - Additional notes and metadata
    """
    
    try:
        # Verify couple exists
        couple = db.query(Couple).filter(Couple.id == log_data.couple_id).first()
        if not couple:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Couple with ID {log_data.couple_id} not found"
            )
        
        # Create log entry
        log = Log(
            couple_id=log_data.couple_id,
            started_at=log_data.started_at,
            ended_at=log_data.ended_at,
            lat=log_data.lat,
            lng=log_data.lng,
            place_type=log_data.place_type,
            place_name=log_data.place_name,
            address=log_data.address,
            spent=log_data.spent,
            currency=log_data.currency,
            note=log_data.note,
            rating=log_data.rating,
            metadata_jsonb=log_data.metadata or {}
        )
        
        db.add(log)
        db.commit()
        db.refresh(log)
        
        logger.info("Log created successfully",
                   log_id=log.id,
                   couple_id=log_data.couple_id,
                   place_type=log_data.place_type)
        
        return log
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error creating log", error=str(e))
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create log entry"
        )


@router.get("/couple/{couple_id}", response_model=List[LogResponse])
async def get_couple_logs(
    couple_id: int,
    limit: int = 50,
    offset: int = 0,
    db: Session = Depends(get_db)
):
    """Get logs for a specific couple with pagination"""
    
    try:
        # Verify couple exists
        couple = db.query(Couple).filter(Couple.id == couple_id).first()
        if not couple:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Couple with ID {couple_id} not found"
            )
        
        # Get logs with pagination
        logs = db.query(Log).filter(
            Log.couple_id == couple_id
        ).order_by(
            Log.started_at.desc()
        ).offset(offset).limit(limit).all()
        
        logger.info("Retrieved couple logs",
                   couple_id=couple_id,
                   count=len(logs),
                   limit=limit,
                   offset=offset)
        
        return logs
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error retrieving couple logs", 
                    couple_id=couple_id, 
                    error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve logs"
        )


@router.get("/{log_id}", response_model=LogResponse)
async def get_log(
    log_id: int,
    db: Session = Depends(get_db)
):
    """Get a specific log by ID"""
    
    try:
        log = db.query(Log).filter(Log.id == log_id).first()
        if not log:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Log with ID {log_id} not found"
            )
        
        return log
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error retrieving log", log_id=log_id, error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve log"
        )


@router.post("/ingest/location", response_model=SuccessResponse)
async def ingest_location_ping(
    location_data: LocationPing,
    db: Session = Depends(get_db)
):
    """
    Ingest location ping data for behavioral analysis (optional endpoint)
    
    This endpoint can be used to collect location data in the background
    to infer when users visit recommended places. Use with appropriate
    privacy policies and user consent.
    """
    
    try:
        # Verify couple exists
        couple = db.query(Couple).filter(Couple.id == location_data.couple_id).first()
        if not couple:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Couple with ID {location_data.couple_id} not found"
            )
        
        # For MVP, we'll just log the location ping
        # In production, you might want to:
        # 1. Store in a separate location_pings table
        # 2. Process for recommendation matching
        # 3. Apply privacy controls and data retention policies
        
        logger.info("Location ping received",
                   couple_id=location_data.couple_id,
                   lat=location_data.lat,
                   lng=location_data.lng,
                   timestamp=location_data.timestamp.isoformat(),
                   activity_type=location_data.activity_type)
        
        return SuccessResponse(
            success=True,
            message="Location ping processed successfully",
            data={
                "couple_id": location_data.couple_id,
                "timestamp": location_data.timestamp.isoformat()
            }
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error processing location ping", 
                    couple_id=location_data.couple_id, 
                    error=str(e))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to process location ping"
        )


@router.put("/{log_id}", response_model=LogResponse)
async def update_log(
    log_id: int,
    log_data: LogCreate,
    db: Session = Depends(get_db)
):
    """Update an existing log entry"""
    
    try:
        log = db.query(Log).filter(Log.id == log_id).first()
        if not log:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Log with ID {log_id} not found"
            )
        
        # Update log fields
        for field, value in log_data.dict(exclude_unset=True).items():
            if field == "metadata":
                setattr(log, "metadata_jsonb", value or {})
            else:
                setattr(log, field, value)
        
        db.commit()
        db.refresh(log)
        
        logger.info("Log updated successfully", log_id=log_id)
        
        return log
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error updating log", log_id=log_id, error=str(e))
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to update log"
        )


@router.delete("/{log_id}", response_model=SuccessResponse)
async def delete_log(
    log_id: int,
    db: Session = Depends(get_db)
):
    """Delete a log entry"""
    
    try:
        log = db.query(Log).filter(Log.id == log_id).first()
        if not log:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Log with ID {log_id} not found"
            )
        
        db.delete(log)
        db.commit()
        
        logger.info("Log deleted successfully", log_id=log_id)
        
        return SuccessResponse(
            success=True,
            message=f"Log {log_id} deleted successfully"
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error("Error deleting log", log_id=log_id, error=str(e))
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete log"
        )
