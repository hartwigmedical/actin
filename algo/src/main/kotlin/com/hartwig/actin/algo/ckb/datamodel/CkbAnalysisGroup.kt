package com.hartwig.actin.algo.ckb.datamodel

data class CkbAnalysisGroup(
    val id: Int,
    val name: String,
    val outcome: String,
    val nPatients: String,
    val endPointMetrics: List<CkbEndPointMetric>,
    val notes: String?
)
