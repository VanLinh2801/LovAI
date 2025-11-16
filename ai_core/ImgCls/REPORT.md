# AI IMAGE CLASSIFICATION SERVICE

## USE CASE
Phân loại ảnh tự động vào 5 nhãn: `boyfriend`, `girlfriend`, `couple`, `landscape`, `noise` (ảnh blur/low quality cần xóa)

## TECH STACK
- **Backend**: FastAPI + Uvicorn (REST API)
- **AI Model**: CoCa ViT-L-14 (OpenCLIP) - Zero-shot classification
- **Image Processing**: OpenCV (blur detection), Pillow
- **Deep Learning**: PyTorch 2.0+
- **Architecture**: Clean Architecture + SOLID principles
- **Deployment**: Docker + Docker Compose

## AI DETAILS
- **Model**: CoCa (Contrastive Captioners) - `mscoco_finetuned_laion2B-s13B-b90k`
- **Hardware**: NVIDIA RTX 5070 Ti (16GB VRAM) - ~100ms/image
- **Quality Check**: Laplacian variance (blur score < 100 → noise)

