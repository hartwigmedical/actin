package com.hartwig.actin.efficacy

data class EndPoint(
    val id: Int,
    val name: String,
    val value: Double?,
    val unitOfMeasure: EndPointUnit,
    val confidenceInterval: ConfidenceInterval?,
    val type: EndPointType,
    val derivedMetrics: List<DerivedMetric>
)