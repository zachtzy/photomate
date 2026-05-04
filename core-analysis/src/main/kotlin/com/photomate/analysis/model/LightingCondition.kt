/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.model

/**
 * Lighting condition analysis for the current scene.
 */
data class LightingCondition(
    val direction: LightDirection,
    val quality: LightQuality,
    val colorTemperature: ColorTemperature,
    /** Estimated dynamic range in EV stops. */
    val dynamicRange: Double,
    /** Overall lighting score (0.0-10.0). */
    val score: Double,
    val suggestions: List<String>,
)

enum class LightDirection(val displayName: String) {
    FRONT("顺光"),
    BACK("逆光"),
    SIDE_LEFT("左侧光"),
    SIDE_RIGHT("右侧光"),
    TOP("顶光"),
    BOTTOM("底光"),
    DIFFUSED("漫射光"),
    UNKNOWN("未知"),
}

enum class LightQuality(val displayName: String) {
    HARD("硬光"),
    SOFT("柔光"),
    MIXED("混合光"),
}

enum class ColorTemperature(val displayName: String, val kelvinRange: IntRange) {
    WARM("暖色调", 2700..3500),
    NEUTRAL("中性色调", 3500..5500),
    COOL("冷色调", 5500..7500),
    DAYLIGHT("日光", 5000..6500),
    GOLDEN("金色调", 2500..3500),
    BLUE("蓝色调", 7500..10000),
}
