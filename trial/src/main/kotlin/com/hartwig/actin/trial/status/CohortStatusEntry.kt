package com.hartwig.actin.trial.status

data class CohortStatusEntry(
    val nctId: String,
    val trialStatus: TrialStatus,
    val cohortId: String,
    val cohortParentId: String? = null,
    val cohortStatus: TrialStatus,
    val cohortSlotsNumberAvailable: Int? = null,
    val cohortSlotsDateUpdate: String? = null
)