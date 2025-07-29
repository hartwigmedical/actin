package com.hartwig.actin.datamodel.clinical

data class PerformanceStatus(
    val latestWho: Int? = null,
    val latestAsa: Int? = null,
    val whoStatuses: List<WhoStatus> = emptyList(),
    val asaScores: List<AsaScore> = emptyList()
)