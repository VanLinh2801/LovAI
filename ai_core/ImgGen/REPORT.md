# AI IMAGE GENERATION SERVICE

## USE CASE
Tạo và chỉnh sửa ảnh couple với 2 chế độ: **Text-to-Image** (tạo từ prompt) và **Image Editing** (biến đổi ảnh từ prompt + ảnh input). Hỗ trợ 8 styles: `romantic`, `vintage`, `modern`, `anime`, `realistic`, `artistic`, `fantasy`, `minimalist`.

## TECH STACK
- **Backend**: FastAPI + Uvicorn (REST API)
- **AI Model**: Google Gemini 2.5 Flash Image - Multimodal generation
- **Image Processing**: Pillow (PIL)
- **Architecture**: Clean Architecture + SOLID principles (4 layers: Domain, Application, Infrastructure, Presentation)
- **Deployment**: Docker + Docker Compose

## AI DETAILS
- **Model**: Gemini 2.5 Flash Image (Google GenAI SDK)
- **Capabilities**: Zero-shot text-to-image, image editing, multi-image input
- **Generation Time**: ~30-60 seconds/image
- **Output**: Base64 encoded PNG images

