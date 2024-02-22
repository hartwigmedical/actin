package com.hartwig.actin.efficacy

data class PrimaryEndPoint(
    val id: Int,
    val name: String,
    val value: Double?,
    val unitOfMeasure: PrimaryEndPointUnit,
    val confidenceInterval: ConfidenceInterval?,
    val type: PrimaryEndPointType,
    val derivedMetrics: List<DerivedMetric>
)