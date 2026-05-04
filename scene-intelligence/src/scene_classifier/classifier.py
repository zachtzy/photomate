# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

"""Scene classifier using MobileNetV3 for fast scene type recognition."""

import numpy as np

# Scene types matching the Kotlin SceneType enum
SCENE_TYPES = [
    "LANDSCAPE", "PORTRAIT", "STREET", "MACRO", "ARCHITECTURE",
    "NIGHT", "GOLDEN_HOUR", "BLUE_HOUR", "FOOD", "SPORTS",
    "WILDLIFE", "INDOOR", "UNDERWATER", "AERIAL", "ABSTRACT",
    "STILL_LIFE", "EVENT", "ASTROPHOTOGRAPHY", "SILHOUETTE", "UNKNOWN",
]


class SceneClassifier:
    """
    Lightweight scene classifier based on MobileNetV3.
    Target inference time: ~5ms on GPU.
    """

    def __init__(self, model_path: str | None = None):
        self.model = None
        self._load_model(model_path)

    def _load_model(self, model_path: str | None):
        """Load the scene classification model."""
        if model_path is None:
            # Use pretrained MobileNetV3 as feature extractor
            # In production, this would be a fine-tuned model
            try:
                import torchvision.models as models
                import torch

                self.model = models.mobilenet_v3_small(pretrained=False)
                # Replace classifier head for our scene types
                self.model.classifier[-1] = torch.nn.Linear(1024, len(SCENE_TYPES))
                self.model.eval()
            except ImportError:
                self.model = None

    def classify(self, frame: np.ndarray) -> dict:
        """
        Classify the scene type from a camera frame.

        Args:
            frame: RGB image as numpy array (H, W, 3)

        Returns:
            Dict with scene_type, confidence, and top candidates.
        """
        if self.model is None:
            # Fallback: return unknown with low confidence
            return {
                "scene_type": "UNKNOWN",
                "confidence": 0.0,
                "top_candidates": [],
            }

        import torch
        from torchvision import transforms

        transform = transforms.Compose([
            transforms.ToPILImage(),
            transforms.Resize((224, 224)),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
        ])

        input_tensor = transform(frame).unsqueeze(0)

        with torch.no_grad():
            output = self.model(input_tensor)
            probabilities = torch.softmax(output, dim=1)[0]

        # Get top-3 predictions
        top_k = torch.topk(probabilities, k=min(3, len(SCENE_TYPES)))
        top_indices = top_k.indices.tolist()
        top_scores = top_k.values.tolist()

        return {
            "scene_type": SCENE_TYPES[top_indices[0]],
            "confidence": top_scores[0],
            "top_candidates": [
                {"scene_type": SCENE_TYPES[idx], "confidence": score}
                for idx, score in zip(top_indices, top_scores)
            ],
        }
