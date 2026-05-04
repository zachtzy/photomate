# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

"""Composition analyzer for evaluating frame composition quality."""

import numpy as np


class CompositionAnalyzer:
    """
    Analyzes composition quality using classical photography rules.
    Evaluates rule-of-thirds, symmetry, and subject positioning.
    """

    def analyze(self, frame: np.ndarray) -> dict:
        """
        Analyze composition quality of the given frame.

        Args:
            frame: RGB image as numpy array (H, W, 3)

        Returns:
            Composition analysis results including scores and subject position.
        """
        h, w = frame.shape[:2]

        # Compute saliency map to find the main subject
        subject_bbox = self._detect_subject(frame)

        # Evaluate rule of thirds
        rot_score = self._evaluate_rule_of_thirds(subject_bbox, w, h)

        # Evaluate symmetry
        symmetry_score = self._evaluate_symmetry(frame)

        return {
            "rule_of_thirds_score": rot_score,
            "symmetry_score": symmetry_score,
            "leading_lines_count": 0,
            "leading_lines_quality": 0.0,
            "main_subject": subject_bbox,
        }

    def _detect_subject(self, frame: np.ndarray) -> dict | None:
        """
        Detect main subject using simple saliency-based approach.
        In production, this would use YOLOv8 or similar.
        """
        import cv2

        gray = cv2.cvtColor(frame, cv2.COLOR_RGB2GRAY)
        h, w = gray.shape

        # Simple frequency-based saliency
        dft = cv2.dft(np.float32(gray), flags=cv2.DFT_COMPLEX_OUTPUT)
        magnitude = cv2.magnitude(dft[:, :, 0], dft[:, :, 1])
        log_magnitude = np.log1p(magnitude)

        # Smooth in frequency domain for spectral residual
        mean_log = cv2.blur(log_magnitude, (3, 3))
        spectral_residual = log_magnitude - mean_log

        # Reconstruct saliency map
        saliency = np.exp(spectral_residual)
        saliency = cv2.GaussianBlur(saliency, (9, 9), 2.5)
        saliency = (saliency - saliency.min()) / (saliency.max() - saliency.min() + 1e-8)

        # Threshold to find salient region
        threshold = 0.6
        binary = (saliency > threshold).astype(np.uint8)

        contours, _ = cv2.findContours(binary, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        if not contours:
            return None

        # Get bounding box of largest contour
        largest = max(contours, key=cv2.contourArea)
        x, y, bw, bh = cv2.boundingRect(largest)

        return {
            "x": x / w,
            "y": y / h,
            "width": bw / w,
            "height": bh / h,
            "label": "subject",
            "confidence": 0.7,
        }

    def _evaluate_rule_of_thirds(self, subject: dict | None, w: int, h: int) -> float:
        """Evaluate how well the subject aligns with rule-of-thirds grid."""
        if subject is None:
            return 0.5

        center_x = subject["x"] + subject["width"] / 2
        center_y = subject["y"] + subject["height"] / 2

        # Distance to nearest third line
        third_xs = [1 / 3, 2 / 3]
        third_ys = [1 / 3, 2 / 3]

        min_x_dist = min(abs(center_x - tx) for tx in third_xs)
        min_y_dist = min(abs(center_y - ty) for ty in third_ys)

        # Score: closer to third lines = higher score
        x_score = max(0, 1 - min_x_dist / 0.2)
        y_score = max(0, 1 - min_y_dist / 0.2)

        return (x_score + y_score) / 2

    def _evaluate_symmetry(self, frame: np.ndarray) -> float:
        """Evaluate bilateral symmetry of the frame."""
        import cv2

        gray = cv2.cvtColor(frame, cv2.COLOR_RGB2GRAY).astype(np.float32)
        h, w = gray.shape

        # Compare left and right halves
        left = gray[:, :w // 2]
        right = cv2.flip(gray[:, w // 2:w // 2 * 2], 1)

        if left.shape != right.shape:
            min_w = min(left.shape[1], right.shape[1])
            left = left[:, :min_w]
            right = right[:, :min_w]

        # Normalized cross-correlation as symmetry measure
        diff = np.abs(left - right)
        max_val = max(gray.max(), 1)
        symmetry = 1 - (diff.mean() / max_val)

        return float(np.clip(symmetry, 0, 1))
