package com.hartwig.actin.datamodel.trial

data class Trial(
    val identification: TrialIdentification,
    val generalEligibility: List<Eligibility>,
    val cohorts: List<Cohort>
)
