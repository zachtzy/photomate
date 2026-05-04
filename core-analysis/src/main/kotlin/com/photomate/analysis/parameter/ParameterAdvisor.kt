/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.parameter

import com.photomate.analysis.model.*

/**
 * Rule-based camera parameter recommendation engine.
 * Recommends ISO, shutter speed, aperture, and white balance based on
 * scene type, lighting conditions, and device capabilities.
 */
class ParameterAdvisor {

    fun recommend(
        sceneType: SceneType,
        lighting: LightingCondition,
        device: DeviceCapabilities,
    ): ParameterSuggestion {
        val iso = recommendIso(sceneType, lighting, device)
        val shutterSpeed = recommendShutterSpeed(sceneType, device)
        val aperture = recommendAperture(sceneType, device)
        val whiteBalance = recommendWhiteBalance(lighting)

        val explanation = buildExplanation(sceneType, lighting)

        return ParameterSuggestion(
            iso = iso,
            shutterSpeed = shutterSpeed,
            aperture = aperture,
            whiteBalance = whiteBalance,
            overallExplanation = explanation,
        )
    }

    private fun recommendIso(
        sceneType: SceneType,
        lighting: LightingCondition,
        device: DeviceCapabilities,
    ): IsoRecommendation {
        val baseIso = when {
            lighting.score >= 8.0 -> 100       // Excellent light
            lighting.score >= 6.0 -> 200       // Good light
            lighting.score >= 4.0 -> 400       // Moderate light
            lighting.score >= 2.0 -> 800       // Low light
            else -> 1600                        // Very low light
        }

        // Adjust for scene type
        val adjusted = when (sceneType) {
            SceneType.NIGHT, SceneType.ASTROPHOTOGRAPHY -> maxOf(baseIso, 1600)
            SceneType.SPORTS, SceneType.WILDLIFE -> maxOf(baseIso, 400)
            SceneType.LANDSCAPE, SceneType.ARCHITECTURE -> minOf(baseIso, 200)
            else -> baseIso
        }

        // Clamp to device range
        val clamped = adjusted.coerceIn(device.isoRange)

        val reason = when {
            clamped <= 200 -> "光线充足，低 ISO 保证画质"
            clamped <= 800 -> "平衡画质与快门速度"
            else -> "弱光环境，提高 ISO 确保曝光"
        }

        return IsoRecommendation(
            value = clamped,
            range = device.isoRange,
            reason = reason,
        )
    }

    private fun recommendShutterSpeed(
        sceneType: SceneType,
        device: DeviceCapabilities,
    ): ShutterSpeedRecommendation {
        // Base: reciprocal rule (1/focal_length for handheld)
        val minHandheld = (device.focalLengthMm * device.sensorSize.cropFactor).toInt()
            .coerceAtLeast(30)

        val denominator = when (sceneType) {
            SceneType.SPORTS, SceneType.WILDLIFE -> maxOf(1000, minHandheld)
            SceneType.STREET -> maxOf(250, minHandheld)
            SceneType.PORTRAIT -> maxOf(125, minHandheld)
            SceneType.LANDSCAPE -> if (device.hasOpticalStabilization) 30 else maxOf(60, minHandheld)
            SceneType.NIGHT -> if (device.hasOpticalStabilization) 15 else 30
            SceneType.ASTROPHOTOGRAPHY -> 20 // 500 rule approximation handled separately
            SceneType.FOOD, SceneType.STILL_LIFE -> maxOf(60, minHandheld)
            else -> maxOf(125, minHandheld)
        }

        val reason = when (sceneType) {
            SceneType.SPORTS, SceneType.WILDLIFE -> "高速快门冻结运动"
            SceneType.LANDSCAPE -> "慢快门可用三脚架增加细节"
            SceneType.NIGHT -> "弱光需要较长曝光" + if (device.hasOpticalStabilization) "（防抖辅助）" else "（建议三脚架）"
            SceneType.PORTRAIT -> "适中快门避免人物模糊"
            else -> "安全快门速度避免手抖"
        }

        return ShutterSpeedRecommendation(
            denominatorValue = denominator,
            displayValue = "1/${denominator}s",
            reason = reason,
        )
    }

    private fun recommendAperture(
        sceneType: SceneType,
        device: DeviceCapabilities,
    ): ApertureRecommendation {
        val fNumber = when (sceneType) {
            SceneType.PORTRAIT -> 2.8
            SceneType.LANDSCAPE, SceneType.ARCHITECTURE -> 8.0
            SceneType.MACRO -> 11.0
            SceneType.STREET -> 5.6
            SceneType.NIGHT -> device.apertureRange.start // Widest available
            SceneType.SPORTS, SceneType.WILDLIFE -> 4.0
            SceneType.ASTROPHOTOGRAPHY -> device.apertureRange.start
            SceneType.FOOD -> 3.5
            SceneType.STILL_LIFE -> 5.6
            else -> 5.6
        }.coerceIn(device.apertureRange)

        val reason = when {
            fNumber <= 2.8 -> "大光圈虚化背景，突出主体"
            fNumber <= 5.6 -> "适中光圈，平衡景深和锐度"
            fNumber <= 8.0 -> "小光圈增大景深，适合风景/建筑"
            else -> "最小光圈确保前后景均清晰"
        }

        return ApertureRecommendation(fNumber = fNumber, reason = reason)
    }

    private fun recommendWhiteBalance(lighting: LightingCondition): WhiteBalanceRecommendation {
        val (kelvin, presetName) = when (lighting.colorTemperature) {
            ColorTemperature.WARM -> 3200 to "钨丝灯"
            ColorTemperature.NEUTRAL -> 4500 to "荧光灯"
            ColorTemperature.COOL -> 6500 to "阴天"
            ColorTemperature.DAYLIGHT -> 5500 to "日光"
            ColorTemperature.GOLDEN -> 3000 to "暖光"
            ColorTemperature.BLUE -> 7500 to "阴影"
        }

        val reason = "匹配当前${lighting.colorTemperature.displayName}环境，保持色彩准确"

        return WhiteBalanceRecommendation(
            kelvin = kelvin,
            presetName = presetName,
            reason = reason,
        )
    }

    private fun buildExplanation(sceneType: SceneType, lighting: LightingCondition): String {
        return "检测到${sceneType.displayName}场景，${lighting.direction.displayName}条件。" +
            "参数已针对${lighting.quality.displayName}环境优化。"
    }
}
