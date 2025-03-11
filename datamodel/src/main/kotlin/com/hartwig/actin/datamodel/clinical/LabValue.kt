package com.hartwig.actin.datamodel.clinical

import java.time.LocalDate

data class LabValue(
    val date: LocalDate,
    val measurement: LabMeasurement,
    val comparator: String,
    val value: Double,
    val unit: LabUnit,
    val refLimitLow: Double? = null,
    val refLimitUp: Double? = null,
    val isOutsideRef: Boolean? = null
)
