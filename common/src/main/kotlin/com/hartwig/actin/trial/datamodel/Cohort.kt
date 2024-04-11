package com.hartwig.actin.trial.datamodel

data class Cohort(
    val metadata: CohortMetadata,
    val eligibility: List<Eligibility>
)
