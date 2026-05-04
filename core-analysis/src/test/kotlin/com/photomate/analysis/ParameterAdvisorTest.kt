/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.parameter

import com.google.common.truth.Truth.assertThat
import com.photomate.analysis.model.*
import org.junit.jupiter.api.Test

class ParameterAdvisorTest {

    private val advisor = ParameterAdvisor()

    private val smartphoneDevice = DeviceCapabilities(
        deviceType = DeviceType.SMARTPHONE,
        isoRange = 50..3200,
        apertureRange = 1.8..16.0,
        hasOpticalStabilization = true,
        sensorSize = SensorSize.SMARTPHONE,
        focalLengthMm = 26.0,
    )

    private val dslrDevice = DeviceCapabilities(
        deviceType = DeviceType.DSLR,
        isoRange = 100..51200,
        apertureRange = 1.4..22.0,
        hasOpticalStabilization = false,
        sensorSize = SensorSize.FULL_FRAME,
        focalLengthMm = 50.0,
    )

    private fun brightLighting() = LightingCondition(
        direction = LightDirection.FRONT,
        quality = LightQuality.SOFT,
        colorTemperature = ColorTemperature.DAYLIGHT,
        dynamicRange = 8.0,
        score = 8.5,
        suggestions = emptyList(),
    )

    private fun lowLighting() = LightingCondition(
        direction = LightDirection.DIFFUSED,
        quality = LightQuality.SOFT,
        colorTemperature = ColorTemperature.WARM,
        dynamicRange = 4.0,
        score = 2.0,
        suggestions = emptyList(),
    )

    @Test
    fun `landscape scene recommends low ISO and small aperture`() {
        val result = advisor.recommend(SceneType.LANDSCAPE, brightLighting(), dslrDevice)

        assertThat(result.iso.value).isAtMost(200)
        assertThat(result.aperture.fNumber).isAtLeast(7.0)
    }

    @Test
    fun `portrait scene recommends wide aperture`() {
        val result = advisor.recommend(SceneType.PORTRAIT, brightLighting(), dslrDevice)

        assertThat(result.aperture.fNumber).isAtMost(4.0)
    }

    @Test
    fun `sports scene recommends fast shutter speed`() {
        val result = advisor.recommend(SceneType.SPORTS, brightLighting(), dslrDevice)

        assertThat(result.shutterSpeed.denominatorValue).isAtLeast(500)
    }

    @Test
    fun `night scene with smartphone recommends high ISO`() {
        val result = advisor.recommend(SceneType.NIGHT, lowLighting(), smartphoneDevice)

        assertThat(result.iso.value).isAtLeast(800)
    }

    @Test
    fun `ISO is clamped to device range`() {
        val result = advisor.recommend(SceneType.ASTROPHOTOGRAPHY, lowLighting(), smartphoneDevice)

        assertThat(result.iso.value).isAtMost(smartphoneDevice.isoRange.last)
        assertThat(result.iso.value).isAtLeast(smartphoneDevice.isoRange.first)
    }

    @Test
    fun `white balance matches lighting color temperature`() {
        val result = advisor.recommend(SceneType.LANDSCAPE, brightLighting(), dslrDevice)

        // Daylight should give ~5500K
        assertThat(result.whiteBalance.kelvin).isEqualTo(5500)
    }

    @Test
    fun `overall explanation mentions scene and lighting`() {
        val result = advisor.recommend(SceneType.STREET, brightLighting(), smartphoneDevice)

        assertThat(result.overallExplanation).contains("街拍")
        assertThat(result.overallExplanation).contains("顺光")
    }

    @Test
    fun `aperture is clamped to device range`() {
        val result = advisor.recommend(SceneType.PORTRAIT, brightLighting(), smartphoneDevice)

        assertThat(result.aperture.fNumber).isAtLeast(smartphoneDevice.apertureRange.start)
        assertThat(result.aperture.fNumber).isAtMost(smartphoneDevice.apertureRange.endInclusive)
    }
}
