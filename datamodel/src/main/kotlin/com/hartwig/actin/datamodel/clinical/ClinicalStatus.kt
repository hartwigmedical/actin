package com.hartwig.actin.datamodel.clinical

data class ClinicalStatus(
    val latestWho: Int? = null,
    val latestAsa: Int? = null,
    val whoStatuses: List<WhoStatus> = emptyList(),
    val asaScores: List<AsaScore> = emptyList(),
    val infectionStatus: InfectionStatus? = null,
    val lvef: Double? = null,
    val hasComplications: Boolean? = null
)