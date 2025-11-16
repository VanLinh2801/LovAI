"""
API Routes - REST endpoints for image classification.
Following Single Responsibility: each endpoint has one clear purpose.
"""
from fastapi import APIRouter, UploadFile, File, HTTPException, status
from typing import List

from app.presentation.dto import (
    ClassificationResponseDTO,
    BatchClassificationResponseDTO,
    HealthResponseDTO,
    CategoriesResponseDTO,
    ImageMetadataDTO
)
from app.domain.entities import ImageData, ImageCategory
from app.config.dependencies import container
from app.config.settings import settings


router = APIRouter(prefix="/api/v1", tags=["classification"])


@router.post(
    "/classify",
    response_model=ClassificationResponseDTO,
    status_code=status.HTTP_200_OK,
    summary="Classify a single image"
)
async def classify_image(file: UploadFile = File(...)):
    """
    Classify a single image into predefined categories.
    
    - **file**: Image file to classify (JPEG, PNG, etc.)
    
    Returns classification result with category, confidence, and metadata.
    """
    # Validate file
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="File must be an image"
        )
    
    # Read file content
    content = await file.read()
    
    # Validate size
    if len(content) > settings.max_image_size_bytes:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"Image size exceeds {settings.max_image_size_mb}MB limit"
        )
    
    # Create image data entity
    image_data = ImageData(
        filename=file.filename or "unknown",
        content=content,
        content_type=file.content_type
    )
    
    try:
        # Execute use case
        use_case = await container.get_classify_use_case()
        result = await use_case.execute(image_data)
        
        # Convert to DTO
        return ClassificationResponseDTO(
            filename=image_data.filename,
            category=result.category.value,
            confidence=result.confidence,
            should_delete=result.should_delete,
            quality_score=result.quality_score,
            metadata=ImageMetadataDTO(
                resolution=result.metadata.resolution,
                width=result.metadata.width,
                height=result.metadata.height,
                format=result.metadata.format,
                size_bytes=result.metadata.size_bytes,
                blur_score=result.metadata.blur_score,
                is_blurry=result.metadata.is_blurry
            )
        )
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Classification failed: {str(e)}"
        )


@router.post(
    "/classify/batch",
    response_model=BatchClassificationResponseDTO,
    status_code=status.HTTP_200_OK,
    summary="Classify multiple images"
)
async def classify_batch(files: List[UploadFile] = File(...)):
    """
    Classify multiple images in batch.
    
    - **files**: List of image files to classify
    
    Returns list of classification results.
    """
    if not files:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No files provided"
        )
    
    # Prepare image data
    images_data = []
    for file in files:
        if not file.content_type or not file.content_type.startswith("image/"):
            continue  # Skip non-image files
        
        content = await file.read()
        if len(content) > settings.max_image_size_bytes:
            continue  # Skip oversized files
        
        images_data.append(ImageData(
            filename=file.filename or "unknown",
            content=content,
            content_type=file.content_type
        ))
    
    if not images_data:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No valid image files provided"
        )
    
    try:
        # Execute batch use case
        use_case = await container.get_batch_use_case()
        results = await use_case.execute(images_data)
        
        # Convert to DTOs
        response_results = []
        for i, result in enumerate(results):
            response_results.append(ClassificationResponseDTO(
                filename=images_data[i].filename,
                category=result.category.value,
                confidence=result.confidence,
                should_delete=result.should_delete,
                quality_score=result.quality_score,
                metadata=ImageMetadataDTO(
                    resolution=result.metadata.resolution,
                    width=result.metadata.width,
                    height=result.metadata.height,
                    format=result.metadata.format,
                    size_bytes=result.metadata.size_bytes,
                    blur_score=result.metadata.blur_score,
                    is_blurry=result.metadata.is_blurry
                )
            ))
        
        return BatchClassificationResponseDTO(
            total=len(response_results),
            results=response_results
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Batch classification failed: {str(e)}"
        )


@router.get(
    "/health",
    response_model=HealthResponseDTO,
    status_code=status.HTTP_200_OK,
    summary="Health check"
)
async def health_check():
    """
    Check service health and model status.
    
    Returns service status and whether the model is loaded.
    """
    try:
        use_case = await container.get_health_check_use_case()
        result = await use_case.execute()
        return HealthResponseDTO(**result)
    except Exception as e:
        return HealthResponseDTO(
            status="unhealthy",
            model_loaded=False
        )


@router.get(
    "/categories",
    response_model=CategoriesResponseDTO,
    status_code=status.HTTP_200_OK,
    summary="Get supported categories"
)
async def get_categories():
    """
    Get list of supported image categories.
    
    Returns list of category names.
    """
    categories = [cat.value for cat in ImageCategory]
    return CategoriesResponseDTO(categories=categories)


