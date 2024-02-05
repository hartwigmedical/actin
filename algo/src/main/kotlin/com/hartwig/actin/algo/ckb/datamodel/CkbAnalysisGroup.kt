package com.hartwig.actin.algo.ckb.datamodel

import com.google.gson.annotations.SerializedName

data class CkbAnalysisGroup(
    val id: Int,
    val name: String,
    val outcome: String,
    @SerializedName("n_patients") val nPatients: String,
    @SerializedName("end_point_metrics") val endPointMetrics: List<CkbEndPointMetric>,
    val notes: String?
)
