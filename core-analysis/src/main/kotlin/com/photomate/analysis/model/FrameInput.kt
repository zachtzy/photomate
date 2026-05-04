/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.model

/**
 * Input data from a single camera frame for analysis.
 */
data class FrameInput(
    /** Compressed frame data (JPEG bytes). */
    val frameData: ByteArray,
    val width: Int,
    val height: Int,
    val device: DeviceCapabilities,
    /** Partial EXIF data from the camera, if available. */
    val currentExif: CurrentExifData?,
    val timestampMs: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrameInput) return false
        return timestampMs == other.timestampMs && width == other.width && height == other.height
    }

    override fun hashCode(): Int {
        var result = timestampMs.hashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}

data class CurrentExifData(
    val iso: Int?,
    val shutterSpeedDenominator: Int?,
    val aperture: Double?,
    val whiteBalanceKelvin: Int?,
    val focalLengthMm: Double?,
    val exposureCompensation: Double?,
)
