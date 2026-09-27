package com.hartwig.actin.algo.ckb.json

import com.fasterxml.jackson.annotation.JsonProperty

data class CkbEndPointMetric(
    val id: Int,
    @JsonProperty("trial_analysis_group_id") val trialAnalysisGroupId: Int,
    @JsonProperty("end_point") val endPoint: CkbEndPoint,
    @JsonProperty("end_point_type") val endPointType: String,
    val value: String,
    @JsonProperty("confidence_interval_95") val confidenceInterval95: String?,
    val numerator: String?,
    val denominator: String?,
    @JsonProperty("derived_metrics") val derivedMetrics: List<CkbDerivedMetric>
)
