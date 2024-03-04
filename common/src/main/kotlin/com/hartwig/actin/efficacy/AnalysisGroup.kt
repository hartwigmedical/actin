package com.hartwig.actin.efficacy

data class AnalysisGroup(
    val id: Int,
    val nPatients: Int,
    val primaryEndPoints: List<PrimaryEndPoint>
)