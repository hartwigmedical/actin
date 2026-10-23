package com.hartwig.actin.algo.ckb.json

import com.fasterxml.jackson.annotation.JsonProperty

data class CkbAnalysisGroup(
    val id: Int,
    val name: String,
    val outcome: String,
    @JsonProperty("n_patients") val nPatients: String,
    @JsonProperty("end_point_metrics") val endPointMetrics: List<CkbEndPointMetric>,
    val notes: String?
)
