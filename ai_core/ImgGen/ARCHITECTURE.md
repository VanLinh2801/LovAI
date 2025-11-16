# Clean Architecture & SOLID - ImgGen

## 🏛️ Clean Architecture

### Layer Dependencies (Dependency Rule)

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│                (FastAPI Routes, DTOs)                    │
│                  app/presentation/                       │
└──────────────────┬──────────────────────────────────────┘
                   │ depends on
┌──────────────────▼──────────────────────────────────────┐
│                   Application Layer                      │
│                      (Use Cases)                         │
│                   app/application/                       │
└──────────────┬──────────────────────────────────────────┘
               │ depends on
┌──────────────▼──────────────────────────────────────────┐
│                     Domain Layer                         │
│             (Entities, Interfaces - Core)                │
│                     app/domain/                          │
└─────────────────────────▲───────────────────────────────┘
                          │ implements
┌─────────────────────────┴───────────────────────────────┐
│                 Infrastructure Layer                     │
│          (Gemini Service, Style Provider)                │
│                 app/infrastructure/                      │
└─────────────────────────────────────────────────────────┘
```

**Quy tắc quan trọng**: Dependencies luôn trỏ vào trong (inward), không bao giờ ngược lại.

## 📐 SOLID Principles

### 1. Single Responsibility Principle (SRP)

Mỗi class chỉ có **một lý do để thay đổi**.

```python
# ✅ Good - Mỗi class có 1 trách nhiệm duy nhất

class GeminiImageService:
    """Chỉ làm việc với Gemini API"""
    def generate(self, request: GenerationRequest) -> GenerationResult:
        ...

class CoupleStyleProvider:
    """Chỉ quản lý styles"""
    def get_styles(self) -> list[dict]:
        ...
    def build_prompt(self, base: str, style: str) -> str:
        ...

class GenerateFromTextUseCase:
    """Chỉ xử lý business logic cho text-to-image"""
    def execute(self, prompt: str, style: str) -> GenerationResult:
        ...
```

### 2. Open/Closed Principle (OCP)

Open for **extension**, closed for **modification**.

```python
# Interface cho phép mở rộng
class IImageGenerator(ABC):
    @abstractmethod
    def generate(self, request: GenerationRequest) -> GenerationResult:
        pass

# Có thể thêm implementation mới mà không sửa code cũ
class GeminiImageService(IImageGenerator):
    def generate(self, request):
        # Gemini implementation
        ...

class StabilityAIService(IImageGenerator):
    def generate(self, request):
        # Stability AI implementation
        ...

# Use case không cần thay đổi khi thêm service mới
class GenerateFromTextUseCase:
    def __init__(self, generator: IImageGenerator):
        self._generator = generator  # Nhận bất kỳ implementation nào
```

### 3. Liskov Substitution Principle (LSP)

Subtype phải có thể **thay thế** được base type.

```python
# Mọi implementation của IImageGenerator đều dùng được
generator1: IImageGenerator = GeminiImageService(api_key)
generator2: IImageGenerator = StabilityAIService(api_key)

# Cả 2 đều hoạt động như nhau với use case
use_case1 = GenerateFromTextUseCase(generator1, style_provider)
use_case2 = GenerateFromTextUseCase(generator2, style_provider)

# Behavior nhất quán
result1 = use_case1.execute("prompt", "style")
result2 = use_case2.execute("prompt", "style")
```

### 4. Interface Segregation Principle (ISP)

Client không nên **depend** vào interfaces không dùng.

```python
# ✅ Good - 2 interfaces riêng biệt, focused
class IImageGenerator(ABC):
    @abstractmethod
    def generate(self, request: GenerationRequest) -> GenerationResult:
        pass

class IStyleProvider(ABC):
    @abstractmethod
    def get_styles(self) -> list[dict]:
        pass
    @abstractmethod
    def build_prompt(self, base: str, style: str) -> str:
        pass

# ❌ Bad - 1 interface quá lớn
class IImageService(ABC):
    @abstractmethod
    def generate(self, request):
        pass
    @abstractmethod
    def get_styles(self):
        pass
    @abstractmethod
    def build_prompt(self, base, style):
        pass
    @abstractmethod
    def upload_image(self, image):
        pass
    # ... quá nhiều methods
```

### 5. Dependency Inversion Principle (DIP)

High-level modules không depend vào low-level modules. **Cả 2 depend vào abstractions**.

```python
# ✅ Good - Use case depend vào interface (abstraction)
class GenerateFromTextUseCase:
    def __init__(
        self, 
        generator: IImageGenerator,      # Interface, not concrete
        style_provider: IStyleProvider   # Interface, not concrete
    ):
        self._generator = generator
        self._style_provider = style_provider

# Dependency Injection tại main.py
gemini_service = GeminiImageService(api_key)        # Concrete
style_provider = CoupleStyleProvider()              # Concrete
use_case = GenerateFromTextUseCase(                 # High-level
    generator=gemini_service,                       # Inject dependencies
    style_provider=style_provider
)
```

## 🔄 Dependency Injection

### Constructor Injection

```python
# main.py - Composition Root
settings = get_settings()

# 1. Create infrastructure (lowest level)
gemini_service = GeminiImageService(api_key=settings.GOOGLE_API_KEY)
style_provider = CoupleStyleProvider()

# 2. Inject vào application layer
text_use_case = GenerateFromTextUseCase(gemini_service, style_provider)
image_use_case = GenerateFromImageUseCase(gemini_service, style_provider)
styles_use_case = GetStylesUseCase(style_provider)

# 3. Inject vào presentation layer
router = create_router(text_use_case, image_use_case, styles_use_case)
app.include_router(router)
```

## 📦 Module Structure

```
app/
├── domain/              # Core Business Logic (innermost)
│   ├── entities.py      # Business entities
│   └── interfaces.py    # Abstract interfaces (contracts)
│
├── application/         # Business Rules & Use Cases
│   └── use_cases.py     # Application business rules
│
├── infrastructure/      # External Services (implements domain interfaces)
│   ├── gemini_service.py    # Gemini API implementation
│   └── style_provider.py    # Style management implementation
│
└── presentation/        # API Layer (outermost)
    ├── schemas.py       # DTOs (Data Transfer Objects)
    └── routes.py        # HTTP endpoints
```

## 🎯 Benefits

### 1. **Testability**
```python
# Mock interfaces dễ dàng
class MockImageGenerator(IImageGenerator):
    def generate(self, request):
        return GenerationResult(image_data=b"fake", prompt_used="test")

# Test use case
def test_generate_from_text():
    mock_gen = MockImageGenerator()
    mock_style = MockStyleProvider()
    use_case = GenerateFromTextUseCase(mock_gen, mock_style)
    result = use_case.execute("prompt", "style")
    assert result.success
```

### 2. **Maintainability**
- Thay đổi Gemini service không ảnh hưởng use cases
- Thêm style mới chỉ sửa style provider
- Thay đổi API format chỉ sửa presentation layer

### 3. **Flexibility**
```python
# Dễ dàng switch implementation
if use_stability:
    generator = StabilityAIService(api_key)
else:
    generator = GeminiImageService(api_key)

# Use case vẫn hoạt động bình thường
use_case = GenerateFromTextUseCase(generator, style_provider)
```

### 4. **Separation of Concerns**
- Domain: Business logic thuần túy
- Application: Orchestration
- Infrastructure: Technical details
- Presentation: User interface

## 🚀 Flow Example

### Text-to-Image Generation Flow

```
1. HTTP Request → Presentation Layer
   routes.py: generate_from_text(request)
   ↓
2. Convert DTO → Application Layer
   use_case.execute(prompt, style)
   ↓
3. Build Prompt → Infrastructure (Style Provider)
   style_provider.build_prompt(prompt, style)
   ↓
4. Create Request → Domain Entity
   GenerationRequest(prompt=full_prompt)
   ↓
5. Generate → Infrastructure (Gemini)
   gemini_service.generate(request)
   ↓
6. Return Result → Domain Entity
   GenerationResult(image_data, prompt_used)
   ↓
7. Convert to DTO → Presentation Layer
   GenerationResponse(success, image_base64)
   ↓
8. HTTP Response → Client
```

## 📚 Further Reading

- Clean Architecture by Robert C. Martin
- SOLID Principles
- Dependency Injection
- Hexagonal Architecture (Ports & Adapters)

---

**Version**: 2.0.0  
**Architecture**: Clean Architecture + SOLID  
**Pattern**: Dependency Injection

