# AI Image Classification Service

Image classification service using CoCa (Contrastive Captioners) model to organize photos into albums.

## Quick Start

```bash
# Build and run with Docker
docker-compose up --build

# Test the API
curl -X POST "http://localhost:8000/api/v1/classify" \
  -F "file=@test_image.jpg"
```

## Categories
- boyfriend
- girlfriend
- couple
- landscape
- noise (blur/low quality images to delete)

## Architecture
Clean Architecture with SOLID principles:
- Domain: Core business entities and interfaces
- Application: Use cases and business logic
- Infrastructure: External dependencies (CoCa model, OpenCV)
- Presentation: FastAPI REST API


