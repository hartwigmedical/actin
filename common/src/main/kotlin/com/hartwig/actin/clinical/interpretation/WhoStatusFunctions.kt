package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision

fun WhoStatus.isAtMost(value: Int) =
    (precision == WhoStatusPrecision.AT_MOST && status <= value) || (precision == WhoStatusPrecision.EXACT && status <= value)

fun WhoStatus.isExactly(value: Int) = precision == WhoStatusPrecision.EXACT && status == value

fun WhoStatus.asText() =
    when (precision) {
        WhoStatusPrecision.EXACT -> "$status"
        WhoStatusPrecision.AT_LEAST -> ">=$status"
        WhoStatusPrecision.AT_MOST -> "<=$status"
    }

fun WhoStatus.asRange(): IntRange {
    return when (precision) {
        WhoStatusPrecision.EXACT -> status..status
        WhoStatusPrecision.AT_MOST -> 0..status
        WhoStatusPrecision.AT_LEAST -> status..5
    }
}