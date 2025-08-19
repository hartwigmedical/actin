package com.hartwig.actin.datamodel.clinical

data class PerformanceStatus(
    val whoStatuses: List<WhoStatus> = emptyList(),
    val asaScores: List<AsaScore> = emptyList()
) {
    
    val latestWho: Int?
        get() = whoStatuses.maxByOrNull(WhoStatus::date)?.status

    val latestAsa: Int?
        get() = asaScores.maxByOrNull(AsaScore::date)?.score

}