package com.hartwig.actin.algo.ckb.datamodel

data class CkbEndPointMetric(
    val id: Int,
    val trialAnalysisGroupId: Int,
    val endPoint: CkbEndPoint,
    val endPointType: String,
    val value: String,
    val confidenceInterval95: String?,
    val numerator: String?,
    val denominator: String?,
    val derivedMetrics: List<CkbDerivedMetric>
)
