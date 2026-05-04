/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.model

/**
 * Camera parameter recommendation based on scene and lighting analysis.
 */
data class ParameterSuggestion(
    val iso: IsoRecommendation,
    val shutterSpeed: ShutterSpeedRecommendation,
    val aperture: ApertureRecommendation,
    val whiteBalance: WhiteBalanceRecommendation,
    val overallExplanation: String,
)

data class IsoRecommendation(
    val value: Int,
    val range: IntRange,
    val reason: String,
)

data class ShutterSpeedRecommendation(
    /** Shutter speed as a fraction denominator (e.g. 250 means 1/250s). */
    val denominatorValue: Int,
    val displayValue: String,
    val reason: String,
)

data class ApertureRecommendation(
    /** f-number value (e.g. 2.8 means f/2.8). */
    val fNumber: Double,
    val reason: String,
)

data class WhiteBalanceRecommendation(
    val kelvin: Int,
    val presetName: String,
    val reason: String,
)
