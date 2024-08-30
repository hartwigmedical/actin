package com.hartwig.actin.datamodel.efficacy

data class AnalysisGroup(
    val id: Int,
    val name: String,
    val nPatients: Int,
    val endPoints: List<EndPoint>
)