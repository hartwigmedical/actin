package com.hartwig.actin.algo.ckb.datamodel

data class DerivedMetric(
    val relativeMetricId: Int,
    val value: Int,
    val type: String,
    val confidenceInterval: ConfidenceInterval,
    val pValue: Double
)