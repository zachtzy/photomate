/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.photomate.analysis.model

/** Recognized scene types for photography guidance. */
enum class SceneType(val displayName: String) {
    LANDSCAPE("风景"),
    PORTRAIT("人像"),
    STREET("街拍"),
    MACRO("微距"),
    ARCHITECTURE("建筑"),
    NIGHT("夜景"),
    GOLDEN_HOUR("黄金时刻"),
    BLUE_HOUR("蓝调时刻"),
    FOOD("美食"),
    SPORTS("运动"),
    WILDLIFE("野生动物"),
    INDOOR("室内"),
    UNDERWATER("水下"),
    AERIAL("航拍"),
    ABSTRACT("抽象"),
    STILL_LIFE("静物"),
    EVENT("活动"),
    ASTROPHOTOGRAPHY("星空"),
    SILHOUETTE("剪影"),
    UNKNOWN("未知"),
}
