package com.hartwig.actin.algo.ckb.datamodel

data class DerivedMetric(
    val relativeMetricId: Int,
    val value: Double?,
    val type: String?,
    val confidenceInterval: ConfidenceInterval?,
    val pValue: String?
)