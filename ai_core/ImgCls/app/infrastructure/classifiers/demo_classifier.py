"""
Demo/Mock Classifier for testing without network access.
This is a fallback when the CoCa model cannot be downloaded.
Uses simple heuristics based on image properties.
"""
import random
from typing import Tuple
from PIL import Image

from app.domain.entities import ImageCategory
from app.domain.interfaces import IImageClassifier


class DemoImageClassifier(IImageClassifier):
    """
    Demo classifier using simple heuristics.
    For demonstration purposes only - not production quality.
    """
    
    def __init__(self):
        self._is_ready = False
    
    async def load_model(self):
        """Initialize the demo classifier."""
        if self._is_ready:
            return
        
        print("=" * 60)
        print("⚠️  DEMO MODE ACTIVE")
        print("=" * 60)
        print("CoCa model could not be loaded (likely network issue).")
        print("Using demo classifier based on image heuristics.")
        print("This demonstrates the architecture - not production accuracy.")
        print("=" * 60)
        
        self._is_ready = True
    
    async def classify(self, image: Image.Image) -> Tuple[ImageCategory, float]:
        """
        Classify image using simple heuristics.
        
        Strategy:
        - Very small images or extreme aspect ratios -> NOISE
        - Wide landscape-oriented images -> LANDSCAPE
        - Portrait images with specific characteristics -> BOYFRIEND/GIRLFRIEND/COUPLE
        """
        if not self._is_ready:
            await self.load_model()
        
        width, height = image.size
        aspect_ratio = width / height if height > 0 else 1.0
        total_pixels = width * height
        
        # Rule 1: Very small images are likely noise
        if total_pixels < 50000 or min(width, height) < 100:
            return ImageCategory.NOISE, 0.85
        
        # Rule 2: Extreme aspect ratios might be noise
        if aspect_ratio > 3.0 or aspect_ratio < 0.33:
            return ImageCategory.LANDSCAPE if aspect_ratio > 1.5 else ImageCategory.NOISE, 0.70
        
        # Rule 3: Landscape-oriented images (wider than tall)
        if aspect_ratio > 1.3:
            # Analyze color variance for landscape detection
            try:
                import numpy as np
                img_array = np.array(image.resize((100, 100)))
                color_variance = img_array.std()
                
                # High variance might indicate natural scenery
                if color_variance > 40:
                    return ImageCategory.LANDSCAPE, 0.75
            except:
                pass
            
            return ImageCategory.LANDSCAPE, 0.65
        
        # Rule 4: Portrait-oriented or square images
        # Use aspect ratio and size to guess
        if aspect_ratio < 0.9:
            # Tall images - likely single person
            category = random.choice([ImageCategory.BOYFRIEND, ImageCategory.GIRLFRIEND])
            return category, 0.60
        else:
            # Square-ish images - could be couple or single person
            # Use width as heuristic - wider might be couple
            if width > 600:
                return ImageCategory.COUPLE, 0.65
            else:
                category = random.choice([ImageCategory.BOYFRIEND, ImageCategory.GIRLFRIEND])
                return category, 0.55
    
    async def is_ready(self) -> bool:
        """Check if classifier is ready."""
        return self._is_ready


