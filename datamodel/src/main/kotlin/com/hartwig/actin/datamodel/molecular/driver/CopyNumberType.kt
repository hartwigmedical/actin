package com.hartwig.actin.datamodel.molecular.driver

enum class CopyNumberType(val isGain: Boolean, val isDeletion: Boolean) {
    FULL_GAIN(true, false),
    PARTIAL_GAIN(true, false),
    DEL(false, true),
    NONE(false, false)
}
