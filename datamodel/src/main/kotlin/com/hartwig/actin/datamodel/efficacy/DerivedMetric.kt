package com.hartwig.actin.datamodel.efficacy

data class DerivedMetric(
    val relativeMetricId: Int,
    val value: Double?,
    val type: String?,
    val confidenceInterval: ConfidenceInterval?,
    val pValue: String?
)