/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.composition

import com.photomate.analysis.model.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Composition rule engine that evaluates frame composition quality
 * based on classical photography rules.
 */
class CompositionAnalyzer {

    /**
     * Analyze composition given a subject position and frame dimensions.
     */
    fun analyze(subject: SubjectPosition?, frameWidth: Int, frameHeight: Int): CompositionScore {
        val ruleOfThirds = analyzeRuleOfThirds(subject, frameWidth, frameHeight)
        val symmetry = analyzeSymmetry(subject)
        val leadingLines = LeadingLinesInfo(count = 0, quality = 0.0, convergeToSubject = false)

        val suggestions = mutableListOf<String>()
        generateSuggestions(ruleOfThirds, symmetry, subject, suggestions)

        val overallScore = calculateOverallScore(ruleOfThirds, symmetry, leadingLines)

        return CompositionScore(
            overallScore = overallScore,
            ruleOfThirds = ruleOfThirds,
            symmetry = symmetry,
            leadingLines = leadingLines,
            subjectPosition = subject,
            suggestions = suggestions,
        )
    }

    private fun analyzeRuleOfThirds(
        subject: SubjectPosition?,
        frameWidth: Int,
        frameHeight: Int,
    ): RuleOfThirdsScore {
        if (subject == null) {
            return RuleOfThirdsScore(
                powerPointAlignment = 0.0,
                horizontalAlignment = 0.0,
                verticalAlignment = 0.0,
                nearestPowerPoint = null,
            )
        }

        val subjectCenterX = subject.x + subject.width / 2
        val subjectCenterY = subject.y + subject.height / 2

        // Power points at intersections of third lines
        val powerPoints = listOf(
            PowerPoint.TOP_LEFT to Pair(1.0 / 3, 1.0 / 3),
            PowerPoint.TOP_RIGHT to Pair(2.0 / 3, 1.0 / 3),
            PowerPoint.BOTTOM_LEFT to Pair(1.0 / 3, 2.0 / 3),
            PowerPoint.BOTTOM_RIGHT to Pair(2.0 / 3, 2.0 / 3),
        )

        // Find nearest power point and calculate alignment
        var minDistance = Double.MAX_VALUE
        var nearestPoint: PowerPoint? = null

        for ((point, coords) in powerPoints) {
            val distance = sqrt(
                (subjectCenterX - coords.first).let { it * it } +
                    (subjectCenterY - coords.second).let { it * it }
            )
            if (distance < minDistance) {
                minDistance = distance
                nearestPoint = point
            }
        }

        // Max possible distance from a power point is ~0.47 (corner to center)
        // Normalize so that distance 0 = score 1.0, distance 0.2+ = score ~0
        val powerPointAlignment = (1.0 - min(minDistance / 0.2, 1.0)).coerceIn(0.0, 1.0)

        // Horizontal alignment: how close subject center Y is to 1/3 or 2/3
        val hThirdDist = min(
            abs(subjectCenterY - 1.0 / 3),
            abs(subjectCenterY - 2.0 / 3),
        )
        val horizontalAlignment = (1.0 - min(hThirdDist / 0.15, 1.0)).coerceIn(0.0, 1.0)

        // Vertical alignment: how close subject center X is to 1/3 or 2/3
        val vThirdDist = min(
            abs(subjectCenterX - 1.0 / 3),
            abs(subjectCenterX - 2.0 / 3),
        )
        val verticalAlignment = (1.0 - min(vThirdDist / 0.15, 1.0)).coerceIn(0.0, 1.0)

        return RuleOfThirdsScore(
            powerPointAlignment = powerPointAlignment,
            horizontalAlignment = horizontalAlignment,
            verticalAlignment = verticalAlignment,
            nearestPowerPoint = nearestPoint,
        )
    }

    private fun analyzeSymmetry(subject: SubjectPosition?): Double {
        if (subject == null) return 0.5 // Assume neutral symmetry without subject info

        val subjectCenterX = subject.x + subject.width / 2
        // Symmetry = how close to center (0.5) horizontally
        val deviationFromCenter = abs(subjectCenterX - 0.5)
        // Score 1.0 when perfectly centered, 0.0 when at edge
        return (1.0 - deviationFromCenter * 2).coerceIn(0.0, 1.0)
    }

    private fun generateSuggestions(
        ruleOfThirds: RuleOfThirdsScore,
        symmetry: Double,
        subject: SubjectPosition?,
        suggestions: MutableList<String>,
    ) {
        if (subject == null) {
            suggestions.add("未检测到明确主体，尝试找到一个视觉焦点")
            return
        }

        val subjectCenterX = subject.x + subject.width / 2
        val subjectCenterY = subject.y + subject.height / 2

        // Rule of thirds suggestions
        if (ruleOfThirds.powerPointAlignment < 0.5) {
            val nearestPoint = ruleOfThirds.nearestPowerPoint
            val direction = when (nearestPoint) {
                PowerPoint.TOP_LEFT -> "左上方"
                PowerPoint.TOP_RIGHT -> "右上方"
                PowerPoint.BOTTOM_LEFT -> "左下方"
                PowerPoint.BOTTOM_RIGHT -> "右下方"
                null -> "三分线交叉点"
            }
            suggestions.add("试着将主体移向$direction 的三分线交叉点")
        }

        // Center composition check - if nearly centered, suggest it's intentional
        if (symmetry > 0.85 && ruleOfThirds.powerPointAlignment < 0.3) {
            suggestions.add("主体接近中心，适合对称构图；若想更动感可偏移至三分线")
        }

        // Edge warning
        if (subjectCenterX < 0.1 || subjectCenterX > 0.9) {
            suggestions.add("主体过于靠近画面边缘，建议留出更多空间")
        }
        if (subjectCenterY < 0.1 || subjectCenterY > 0.9) {
            suggestions.add("主体过于靠近画面顶部/底部，调整取景角度")
        }

        // Headroom for portraits (subject in upper portion)
        if (subjectCenterY < 0.25 && subject.height < 0.5) {
            suggestions.add("主体上方空间较少，可以稍微下移镜头")
        }
    }

    private fun calculateOverallScore(
        ruleOfThirds: RuleOfThirdsScore,
        symmetry: Double,
        leadingLines: LeadingLinesInfo,
    ): Double {
        // Weighted score: rule of thirds is most important
        val rotScore = (ruleOfThirds.powerPointAlignment * 0.5 +
            ruleOfThirds.horizontalAlignment * 0.25 +
            ruleOfThirds.verticalAlignment * 0.25)

        val baseScore = rotScore * 0.6 + symmetry * 0.2 + leadingLines.quality * 0.2

        // Scale to 0-10
        return (baseScore * 10).coerceIn(0.0, 10.0)
    }
}
