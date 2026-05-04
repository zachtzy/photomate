/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.model

/**
 * Result of composition analysis on a camera frame.
 *
 * @property overallScore 0.0-10.0 overall composition quality
 * @property ruleOfThirds alignment score for rule-of-thirds grid (0.0-1.0)
 * @property symmetry symmetry score (0.0-1.0)
 * @property leadingLines detected leading lines count and quality
 * @property subjectPosition where the main subject is located in the frame
 * @property suggestions actionable composition improvement tips
 */
data class CompositionScore(
    val overallScore: Double,
    val ruleOfThirds: RuleOfThirdsScore,
    val symmetry: Double,
    val leadingLines: LeadingLinesInfo,
    val subjectPosition: SubjectPosition?,
    val suggestions: List<String>,
)

data class RuleOfThirdsScore(
    /** How well the subject aligns with the 4 power points (0.0-1.0). */
    val powerPointAlignment: Double,
    /** How well horizontal elements align with horizontal third lines (0.0-1.0). */
    val horizontalAlignment: Double,
    /** How well vertical elements align with vertical third lines (0.0-1.0). */
    val verticalAlignment: Double,
    /** The nearest power point to the main subject. */
    val nearestPowerPoint: PowerPoint?,
)

enum class PowerPoint {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
}

data class LeadingLinesInfo(
    val count: Int,
    /** Average quality of detected leading lines (0.0-1.0). */
    val quality: Double,
    /** Whether leading lines converge toward the subject. */
    val convergeToSubject: Boolean,
)

/**
 * Normalized position of the main subject in the frame.
 * (0,0) = top-left, (1,1) = bottom-right.
 */
data class SubjectPosition(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)
