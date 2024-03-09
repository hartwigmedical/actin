package com.hartwig.actin.trial.datamodel

data class Trial(
    val identification: TrialIdentification,
    val generalEligibility: List<Eligibility>,
    val cohorts: List<Cohort>
)
