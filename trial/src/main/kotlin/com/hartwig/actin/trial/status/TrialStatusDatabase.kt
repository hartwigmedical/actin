package com.hartwig.actin.trial.status

data class TrialStatusDatabase(
    val entries: List<TrialStatusEntry>,
    val studyMETCsToIgnore: Set<String>,
    val unmappedCohortIds: Set<String>,
    val studiesNotInTrialStatusDatabase: Set<String>
)