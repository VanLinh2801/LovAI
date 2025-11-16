"""
CoCa Model Classifier Implementation.
Implements IImageClassifier interface using OpenCLIP's CoCa model.
Following Dependency Inversion: implements abstraction from domain layer.
"""
import torch
import open_clip
from PIL import Image
from typing import Tuple, Optional, Dict

from app.domain.entities import ImageCategory
from app.domain.interfaces import IImageClassifier


class CoCaImageClassifier(IImageClassifier):
    """
    CoCa (Contrastive Captioners) based image classifier.
    Uses zero-shot classification with text prompts.
    """
    
    # Text prompts for each category (carefully crafted for CoCa)
    CATEGORY_PROMPTS: Dict[ImageCategory, str] = {
        ImageCategory.BOYFRIEND: "a photo of a single young man, male person, boyfriend",
        ImageCategory.GIRLFRIEND: "a photo of a single young woman, female person, girlfriend",
        ImageCategory.COUPLE: "a photo of a couple, two people together, romantic couple, man and woman together",
        ImageCategory.LANDSCAPE: "a landscape photo, nature scene, outdoor scenery, mountains, beach, forest, cityscape",
        ImageCategory.NOISE: "a blurry photo, low quality image, noisy image, unclear photo, bad photo"
    }
    
    def __init__(
        self,
        model_name: str = "coca_ViT-L-14",
        pretrained: str = "mscoco_finetuned_laion2B-s13B-b90k",
        device: Optional[str] = None
    ):
        """
        Initialize CoCa classifier.
        
        Args:
            model_name: OpenCLIP model name
            pretrained: Pretrained weights identifier
            device: Device to run model on ('cuda', 'cpu', or None for auto)
        """
        self._model_name = model_name
        self._pretrained = pretrained
        self._device = device or ("cuda" if torch.cuda.is_available() else "cpu")
        self._model = None
        self._preprocess = None
        self._tokenizer = None
        self._text_features = None
        self._is_ready = False
    
    async def load_model(self):
        """Load the CoCa model and prepare text embeddings."""
        if self._is_ready:
            return
        
        try:
            print(f"Loading CoCa model: {self._model_name} with pretrained: {self._pretrained}")
            print("This may take a few minutes on first run to download model weights...")
            
            # Load model with timeout (30 seconds max)
            import asyncio
            try:
                # Try to load with a timeout
                model_load_task = asyncio.create_task(asyncio.to_thread(
                    lambda: open_clip.create_model_and_transforms(
                        self._model_name,
                        pretrained=self._pretrained,
                        cache_dir="/root/.cache/open_clip"
                    )
                ))
                self._model, _, self._preprocess = await asyncio.wait_for(model_load_task, timeout=30.0)
            except asyncio.TimeoutError:
                print("Model loading timed out after 30 seconds")
                raise RuntimeError("Model download/loading timed out - likely network issue")
            
            self._model = self._model.to(self._device)
            self._model.eval()
            
            # Load tokenizer
            self._tokenizer = open_clip.get_tokenizer(self._model_name)
            
            # Pre-compute text features for all categories
            await self._precompute_text_features()
            
            self._is_ready = True
            print(f"✓ CoCa model loaded successfully on {self._device}")
            
        except Exception as e:
            print(f"✗ Failed to load CoCa model: {str(e)}")
            print("  Possible reasons:")
            print("  - Network connectivity issues")
            print("  - HuggingFace Hub is unavailable")
            print("  - Model weights not found")
            raise RuntimeError(f"Model loading failed: {str(e)}")
    
    async def _precompute_text_features(self):
        """Pre-compute and cache text embeddings for all category prompts."""
        texts = [self.CATEGORY_PROMPTS[cat] for cat in ImageCategory]
        text_tokens = self._tokenizer(texts).to(self._device)
        
        with torch.no_grad():
            self._text_features = self._model.encode_text(text_tokens)
            self._text_features /= self._text_features.norm(dim=-1, keepdim=True)
    
    async def classify(self, image: Image.Image) -> Tuple[ImageCategory, float]:
        """
        Classify image using CoCa zero-shot classification.
        
        Args:
            image: PIL Image object
            
        Returns:
            Tuple of (category, confidence)
        """
        if not self._is_ready:
            await self.load_model()
        
        # Preprocess image
        image_tensor = self._preprocess(image).unsqueeze(0).to(self._device)
        
        # Compute image features
        with torch.no_grad():
            image_features = self._model.encode_image(image_tensor)
            image_features /= image_features.norm(dim=-1, keepdim=True)
            
            # Calculate similarity with all category prompts
            similarities = (100.0 * image_features @ self._text_features.T).softmax(dim=-1)
        
        # Get best matching category
        probs = similarities[0].cpu().numpy()
        best_idx = probs.argmax()
        confidence = float(probs[best_idx])
        
        category = list(ImageCategory)[best_idx]
        
        return category, confidence
    
    async def is_ready(self) -> bool:
        """Check if model is loaded and ready."""
        return self._is_ready

