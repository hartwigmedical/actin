package com.hartwig.actin.molecular.datamodel.hmf.driver

enum class CopyNumberType(val isGain: Boolean, val isLoss: Boolean) {
    FULL_GAIN(true, false),
    PARTIAL_GAIN(true, false),
    LOSS(false, true),
    NONE(false, false)
}
