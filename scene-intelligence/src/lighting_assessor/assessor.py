# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

"""Lighting assessor for evaluating scene lighting conditions."""

import numpy as np


class LightingAssessor:
    """
    Analyzes lighting conditions from a camera frame.
    Assesses light direction, quality, color temperature, and dynamic range.
    """

    def assess(self, frame: np.ndarray) -> dict:
        """
        Assess lighting conditions of the given frame.

        Args:
            frame: RGB image as numpy array (H, W, 3)

        Returns:
            Lighting analysis including direction, quality, color temperature.
        """
        direction = self._detect_direction(frame)
        quality = self._assess_quality(frame)
        color_temp = self._estimate_color_temperature(frame)
        dynamic_range = self._estimate_dynamic_range(frame)
        score = self._compute_score(direction, quality, dynamic_range)

        return {
            "direction": direction,
            "quality": quality,
            "estimated_color_temp_kelvin": color_temp,
            "dynamic_range_ev": dynamic_range,
            "overall_score": score,
        }

    def _detect_direction(self, frame: np.ndarray) -> str:
        """
        Detect dominant light direction by analyzing brightness distribution.
        """
        import cv2

        gray = cv2.cvtColor(frame, cv2.COLOR_RGB2GRAY).astype(np.float32)
        h, w = gray.shape

        # Divide frame into quadrants and compute mean brightness
        top = gray[:h // 2, :].mean()
        bottom = gray[h // 2:, :].mean()
        left = gray[:, :w // 2].mean()
        right = gray[:, w // 2:].mean()
        center = gray[h // 4:3 * h // 4, w // 4:3 * w // 4].mean()

        # Determine direction based on brightness distribution
        overall_mean = gray.mean()
        threshold = overall_mean * 0.15

        if abs(top - bottom) < threshold and abs(left - right) < threshold:
            return "DIFFUSED"
        elif top - bottom > threshold:
            return "TOP"
        elif bottom - top > threshold:
            return "BOTTOM"
        elif right - left > threshold:
            return "SIDE_RIGHT"
        elif left - right > threshold:
            return "SIDE_LEFT"
        elif center > overall_mean + threshold:
            return "FRONT"
        elif center < overall_mean - threshold:
            return "BACK"
        else:
            return "DIFFUSED"

    def _assess_quality(self, frame: np.ndarray) -> str:
        """
        Assess light quality (hard vs soft) based on contrast analysis.
        Hard light: high local contrast, sharp shadows.
        Soft light: low local contrast, gradual transitions.
        """
        import cv2

        gray = cv2.cvtColor(frame, cv2.COLOR_RGB2GRAY)

        # Compute local contrast using Laplacian
        laplacian = cv2.Laplacian(gray, cv2.CV_64F)
        contrast_variance = laplacian.var()

        # Thresholds determined empirically
        if contrast_variance > 2000:
            return "HARD"
        elif contrast_variance < 500:
            return "SOFT"
        else:
            return "MIXED"

    def _estimate_color_temperature(self, frame: np.ndarray) -> int:
        """
        Estimate color temperature in Kelvin from white balance of the frame.
        Uses the blue-to-red ratio as a proxy.
        """
        # Mean RGB values
        r_mean = frame[:, :, 0].mean()
        g_mean = frame[:, :, 1].mean()
        b_mean = frame[:, :, 2].mean()

        # Simple color temperature estimation
        # Higher blue ratio = cooler (higher Kelvin)
        # Higher red ratio = warmer (lower Kelvin)
        if r_mean + b_mean == 0:
            return 5500

        ratio = b_mean / (r_mean + 1e-8)

        # Map ratio to kelvin range [2500, 10000]
        # ratio ~0.7 → warm (3000K), ~1.0 → neutral (5500K), ~1.3 → cool (7500K)
        kelvin = int(2500 + (ratio - 0.5) * 10000)
        return max(2500, min(10000, kelvin))

    def _estimate_dynamic_range(self, frame: np.ndarray) -> float:
        """
        Estimate dynamic range in EV stops.
        """
        import cv2

        gray = cv2.cvtColor(frame, cv2.COLOR_RGB2GRAY).astype(np.float32)

        # Use percentiles to avoid noise
        low = np.percentile(gray, 2)
        high = np.percentile(gray, 98)

        if low <= 0:
            low = 1

        # Convert to EV range (log2 of ratio)
        ev_range = np.log2(max(high / low, 1))
        return float(min(ev_range, 14.0))  # Cap at 14 EV

    def _compute_score(self, direction: str, quality: str, dynamic_range: float) -> float:
        """
        Compute overall lighting score (0-10).
        Good lighting: soft, directional, moderate dynamic range.
        """
        score = 5.0

        # Soft light is generally more flattering
        if quality == "SOFT":
            score += 2.0
        elif quality == "MIXED":
            score += 1.0

        # Side light adds dimension
        if direction in ("SIDE_LEFT", "SIDE_RIGHT"):
            score += 1.5
        elif direction == "FRONT":
            score += 1.0
        elif direction == "BACK":
            score -= 0.5  # Backlighting is challenging

        # Moderate dynamic range is ideal (4-8 EV)
        if 4 <= dynamic_range <= 8:
            score += 1.5
        elif dynamic_range > 10:
            score -= 1.0  # Too much contrast

        return max(0.0, min(10.0, score))
