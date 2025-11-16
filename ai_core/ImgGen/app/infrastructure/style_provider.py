"""Style provider implementation"""
from ..domain.interfaces import IStyleProvider


class CoupleStyleProvider(IStyleProvider):
    """Couple image style provider"""
    
    STYLES = {
        "romantic": "romantic lighting, soft focus, warm colors, intimate atmosphere, dreamy",
        "vintage": "vintage photography, sepia tones, classic film grain, retro aesthetic",
        "modern": "modern photography, clean lines, vibrant colors, professional lighting",
        "anime": "anime style, manga art, vibrant anime colors, expressive eyes",
        "realistic": "photorealistic, highly detailed, 8k resolution, realistic",
        "artistic": "artistic painting, oil painting style, impressionist, masterpiece",
        "fantasy": "fantasy art, magical atmosphere, ethereal lighting, mystical",
        "minimalist": "minimalist style, simple composition, clean, elegant"
    }
    
    def get_styles(self) -> list[dict]:
        """Get all available styles"""
        return [
            {"id": style_id, "name": style_id.title(), "prompt": prompt}
            for style_id, prompt in self.STYLES.items()
        ]
    
    def build_prompt(self, base_prompt: str, style: str) -> str:
        """Build complete prompt with style"""
        style_addition = self.STYLES.get(style, self.STYLES["romantic"])
        return f"{base_prompt}. Style: {style_addition}. High quality, detailed."

