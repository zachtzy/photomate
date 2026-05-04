/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.model

/**
 * Device capabilities reported by the client, used to tailor parameter recommendations.
 */
data class DeviceCapabilities(
    val deviceType: DeviceType,
    val isoRange: IntRange,
    val apertureRange: ClosedFloatingPointRange<Double>,
    val hasOpticalStabilization: Boolean,
    val sensorSize: SensorSize,
    val focalLengthMm: Double,
)

enum class DeviceType {
    SMARTPHONE,
    MIRRORLESS,
    DSLR,
    COMPACT,
    ACTION_CAMERA,
}

enum class SensorSize(val cropFactor: Double) {
    FULL_FRAME(1.0),
    APS_C(1.5),
    MICRO_FOUR_THIRDS(2.0),
    ONE_INCH(2.7),
    SMARTPHONE(6.0),
}
