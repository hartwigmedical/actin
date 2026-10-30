package com.hartwig.actin.report.pdf.util

import com.itextpdf.kernel.colors.DeviceRgb

enum class PaletteColor(val default: DeviceRgb) {
    WHITE(DeviceRgb(255, 255, 255)),
    BLACK(DeviceRgb(0, 0, 0)),
    DARK_GREY(DeviceRgb(39, 47, 50)),
    MID_GREY(DeviceRgb(101, 106, 108)),
    BLUE(DeviceRgb(74, 134, 232)),
    EVALUATION_PASS(DeviceRgb(0, 150, 0)),
    EVALUATION_WARN(DeviceRgb(255, 130, 0)),
    EVALUATION_FAILED(DeviceRgb(231, 85, 85)),
    EVALUATION_UNCLEAR(DeviceRgb(85, 85, 85)),
    GREEN(DeviceRgb(0, 150, 0)),
    RED(DeviceRgb(232, 60, 55)),
    YES_OR_NO_UNCLEAR(DeviceRgb(85, 85, 85));

    val key = name.lowercase()
}
