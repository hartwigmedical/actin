package com.hartwig.actin.trial.status

data class TrialStatusDatabase(
    val entries: List<CohortStatusEntry>,
    val studyMETCsToIgnore: Set<String>,
    val unmappedCohortIds: Set<String>,
    val studiesNotInTrialStatusDatabase: Set<String>
)