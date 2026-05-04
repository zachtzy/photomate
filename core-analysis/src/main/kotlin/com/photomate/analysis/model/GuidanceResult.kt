/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.model

/**
 * Aggregated guidance result returned to the client for real-time display.
 */
data class GuidanceResult(
    val composition: CompositionScore,
    val lighting: LightingCondition,
    val parameters: ParameterSuggestion,
    val sceneType: SceneType,
    /** Top priority tip to display prominently. */
    val primaryTip: String,
    /** Additional tips sorted by priority. */
    val secondaryTips: List<String>,
    /** Processing latency in milliseconds. */
    val latencyMs: Long,
)
