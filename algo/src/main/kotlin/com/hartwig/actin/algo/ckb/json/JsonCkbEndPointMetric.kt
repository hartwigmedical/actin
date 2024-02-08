package com.hartwig.actin.algo.ckb.json

import com.google.gson.annotations.SerializedName

data class JsonCkbEndPointMetric(
    val id: Int,
    @SerializedName("trial_analysis_group_id") val trialAnalysisGroupId: Int,
    @SerializedName("end_point") val endPoint: JsonCkbEndPoint,
    @SerializedName("end_point_type") val endPointType: String,
    val value: String,
    @SerializedName("confidence_interval_95") val confidenceInterval95: String?,
    val numerator: String?,
    val denominator: String?,
    @SerializedName("derived_metrics") val derivedMetrics: List<JsonCkbDerivedMetric>
)
