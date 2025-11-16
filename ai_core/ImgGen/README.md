# ImgGen - Couple Image Generator (Clean Architecture)

Backend API service cho việc tạo ảnh couple sử dụng **Google Gemini 2.5 Flash Image** với Clean Architecture và SOLID principles.

## 🏗️ Architecture

```
ImgGen/
├── app/
│   ├── domain/              # Entities & Interfaces (Business Logic)
│   │   ├── entities.py      # Domain entities
│   │   └── interfaces.py    # Abstract interfaces
│   ├── application/         # Use Cases (Business Rules)
│   │   └── use_cases.py     # Application use cases
│   ├── infrastructure/      # External Services (Implementations)
│   │   ├── gemini_service.py    # Gemini API integration
│   │   └── style_provider.py    # Style management
│   └── presentation/        # API Layer (Controllers)
│       ├── schemas.py       # Request/Response DTOs
│       └── routes.py        # API endpoints
├── input/                   # Input images
├── output/                  # Generated images
├── main.py                  # Application entry point
└── requirements.txt
```

## ✨ Features

### 2 Chế Độ Generation:

1. **Text-to-Image**: Tạo ảnh từ prompt text
2. **Image Editing**: Chỉnh sửa/biến đổi ảnh từ prompt + ảnh input

### 8 Styles:
- romantic, vintage, modern, anime
- realistic, artistic, fantasy, minimalist

## 🚀 Quick Start

### 1. Setup Environment

```bash
# Copy và config API key
cp .env.example .env
# Edit .env và thêm GOOGLE_API_KEY của bạn
```

### 2. Run Application

**Option A: Docker (Recommended)**
```bash
docker compose up -d
```

**Option B: Local Development**
```bash
# Install dependencies
uv venv && source .venv/bin/activate
uv pip install -r requirements.txt

# Run server
export GOOGLE_API_KEY=your_key
python main.py
```

### 3. Access API
- **API Docs**: http://localhost:6001/docs
- **Health**: http://localhost:6001/api/health
- **Styles**: http://localhost:6001/api/styles

## 📚 API Endpoints

### 1. Text-to-Image Generation

```bash
curl -X POST "http://localhost:6001/api/generate/text" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "A romantic couple: young man with short hair and young woman with long hair, in a garden at sunset",
    "style": "romantic"
  }' | jq -r '.image_base64' | base64 -d > output/text_gen.png
```

### 2. Image Editing

```bash
curl -X POST "http://localhost:6001/api/generate/edit" \
  -F "prompt=Transform this couple into anime style with cherry blossoms" \
  -F "style=anime" \
  -F "images=@input/couple1.jpg" \
  | jq -r '.image_base64' | base64 -d > output/edited.png
```

### 3. Multiple Images

```bash
curl -X POST "http://localhost:6001/api/generate/edit" \
  -F "prompt=Create artistic collage of these couples" \
  -F "style=artistic" \
  -F "images=@input/couple1.jpg" \
  -F "images=@input/couple2.jpg" \
  | jq -r '.image_base64' | base64 -d > output/collage.png
```

### 4. Get Styles

```bash
curl "http://localhost:6001/api/styles" | jq
```

## 🧪 Testing

### Auto Test Script

```bash
./test_curl.sh
```

Script sẽ test:
- Health check
- Styles endpoint
- Text-to-image generation
- Image editing với couple1.jpg
- Image editing với couple2.jpg
- Multiple images editing

Kết quả được lưu trong `output/` folder.

## 🎯 Clean Architecture & SOLID

### Layers

1. **Domain** (Core): Business entities và interfaces
   - Không depend vào layer nào khác
   - Pure business logic

2. **Application**: Use cases và business rules
   - Depend vào Domain
   - Orchestrate business flows

3. **Infrastructure**: External services implementation
   - Implement Domain interfaces
   - Gemini API, Style provider

4. **Presentation**: API endpoints và DTOs
   - Depend vào Application
   - Handle HTTP requests/responses

### SOLID Principles

- **S**ingle Responsibility: Mỗi class có 1 nhiệm vụ duy nhất
- **O**pen/Closed: Mở rộng qua interfaces, không modify code
- **L**iskov Substitution: Implementations có thể thay thế interfaces
- **I**nterface Segregation: Interfaces nhỏ, focused
- **D**ependency Inversion: Depend vào abstractions, không concrete

## 📁 Project Structure

```
ImgGen/
├── app/
│   ├── domain/
│   │   ├── entities.py          # GenerationRequest, GenerationResult
│   │   └── interfaces.py        # IImageGenerator, IStyleProvider
│   ├── application/
│   │   └── use_cases.py         # GenerateFromText/Image, GetStyles
│   ├── infrastructure/
│   │   ├── gemini_service.py    # GeminiImageService
│   │   └── style_provider.py    # CoupleStyleProvider
│   └── presentation/
│       ├── schemas.py           # Pydantic models
│       └── routes.py            # FastAPI routes
├── input/                       # Input images folder
│   ├── couple1.jpg
│   └── couple2.jpg
├── output/                      # Generated images folder
├── main.py                      # App entry with DI
├── Dockerfile
├── docker-compose.yml
├── requirements.txt
└── test_curl.sh                 # Test script
```

## 🔧 Configuration

### Environment Variables

```env
GOOGLE_API_KEY=your_key_here
HOST=0.0.0.0
PORT=6001
```

## 💻 Example Code

### Python Client

```python
import requests
import base64

# Text-to-image
response = requests.post(
    "http://localhost:6001/api/generate/text",
    json={
        "prompt": "Romantic couple in garden at sunset",
        "style": "romantic"
    }
)
result = response.json()
image_data = base64.b64decode(result['image_base64'])
with open('output.png', 'wb') as f:
    f.write(image_data)

# Image editing
with open('input/couple1.jpg', 'rb') as f:
    response = requests.post(
        "http://localhost:6001/api/generate/edit",
        data={'prompt': 'Make anime style', 'style': 'anime'},
        files={'images': f}
    )
```

## 🎨 Available Styles

| Style | Description |
|-------|-------------|
| romantic | Romantic lighting, soft focus, warm colors |
| vintage | Sepia tones, classic film grain |
| modern | Clean lines, vibrant colors |
| anime | Manga art, vibrant anime colors |
| realistic | Photorealistic, highly detailed |
| artistic | Oil painting style, impressionist |
| fantasy | Magical atmosphere, ethereal |
| minimalist | Simple composition, elegant |

## 🐛 Troubleshooting

### Port 6001 đang được sử dụng

```bash
# Tìm và kill process
lsof -ti:6001 | xargs kill -9
# Hoặc đổi port trong docker-compose.yml
```

### API Key không hoạt động

1. Check API key tại: https://aistudio.google.com/app/apikey
2. Verify key trong `.env` file
3. Restart container: `docker compose restart`

## 📊 Response Format

```json
{
  "success": true,
  "image_base64": "iVBORw0KGgoAAAANSUhEUg...",
  "prompt_used": "Full prompt with style...",
  "error": null
}
```

## 🚀 Deployment

### Docker

```bash
docker compose up -d
docker compose logs -f
docker compose down
```

### Production

1. Set production environment variables
2. Configure reverse proxy (nginx)
3. Add rate limiting
4. Setup monitoring

## 📝 Notes

- Generation time: 30-60 seconds
- Images lưu local ở `output/` folder
- Support multiple images upload
- Base64 encoding cho image response

---

**Version**: 2.0.0 (Clean Architecture)  
**Framework**: FastAPI  
**AI Model**: Google Gemini 2.5 Flash Image  
**Architecture**: Clean Architecture + SOLID
