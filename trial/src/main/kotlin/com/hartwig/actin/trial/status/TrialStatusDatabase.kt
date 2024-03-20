package com.hartwig.actin.trial.status

data class TrialStatusDatabase(
    val entries: List<TrialStatusEntry>,
    val studyMETCsToIgnore: Set<String>,
    val unmappedCohortIds: Set<Int>,
    val studiesNotInTrialStatusDatabase: Set<String>
)