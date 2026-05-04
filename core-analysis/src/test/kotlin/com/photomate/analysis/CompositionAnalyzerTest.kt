/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.composition

import com.google.common.truth.Truth.assertThat
import com.photomate.analysis.model.PowerPoint
import com.photomate.analysis.model.SubjectPosition
import org.junit.jupiter.api.Test

class CompositionAnalyzerTest {

    private val analyzer = CompositionAnalyzer()

    @Test
    fun `subject on top-left power point scores high rule-of-thirds`() {
        // Subject centered at approximately (1/3, 1/3) — top-left power point
        val subject = SubjectPosition(x = 0.28, y = 0.28, width = 0.1, height = 0.1)
        val result = analyzer.analyze(subject, frameWidth = 1920, frameHeight = 1080)

        assertThat(result.ruleOfThirds.powerPointAlignment).isGreaterThan(0.7)
        assertThat(result.ruleOfThirds.nearestPowerPoint).isEqualTo(PowerPoint.TOP_LEFT)
    }

    @Test
    fun `subject in dead center has low rule-of-thirds but high symmetry`() {
        val subject = SubjectPosition(x = 0.45, y = 0.45, width = 0.1, height = 0.1)
        val result = analyzer.analyze(subject, frameWidth = 1920, frameHeight = 1080)

        assertThat(result.symmetry).isGreaterThan(0.8)
        // Center is far from any power point
        assertThat(result.ruleOfThirds.powerPointAlignment).isLessThan(0.5)
    }

    @Test
    fun `subject at edge generates warning suggestion`() {
        val subject = SubjectPosition(x = 0.92, y = 0.5, width = 0.05, height = 0.1)
        val result = analyzer.analyze(subject, frameWidth = 1920, frameHeight = 1080)

        assertThat(result.suggestions).isNotEmpty()
        assertThat(result.suggestions.any { it.contains("边缘") }).isTrue()
    }

    @Test
    fun `no subject detected generates helpful suggestion`() {
        val result = analyzer.analyze(subject = null, frameWidth = 1920, frameHeight = 1080)

        assertThat(result.suggestions).isNotEmpty()
        assertThat(result.suggestions.first()).contains("主体")
    }

    @Test
    fun `overall score is within 0-10 range`() {
        val subject = SubjectPosition(x = 0.3, y = 0.3, width = 0.1, height = 0.1)
        val result = analyzer.analyze(subject, frameWidth = 1920, frameHeight = 1080)

        assertThat(result.overallScore).isAtLeast(0.0)
        assertThat(result.overallScore).isAtMost(10.0)
    }

    @Test
    fun `bottom-right power point alignment`() {
        val subject = SubjectPosition(x = 0.61, y = 0.61, width = 0.1, height = 0.1)
        val result = analyzer.analyze(subject, frameWidth = 1920, frameHeight = 1080)

        assertThat(result.ruleOfThirds.nearestPowerPoint).isEqualTo(PowerPoint.BOTTOM_RIGHT)
        assertThat(result.ruleOfThirds.powerPointAlignment).isGreaterThan(0.5)
    }
}
