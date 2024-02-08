package com.hartwig.actin.algo.ckb.json

import com.google.gson.annotations.SerializedName

data class JsonCkbAnalysisGroup(
    val id: Int,
    val name: String,
    val outcome: String,
    @SerializedName("n_patients") val nPatients: String,
    @SerializedName("end_point_metrics") val endPointMetrics: List<JsonCkbEndPointMetric>,
    val notes: String?
)
