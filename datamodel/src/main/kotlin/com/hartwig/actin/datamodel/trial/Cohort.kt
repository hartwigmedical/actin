package com.hartwig.actin.datamodel.trial

data class Cohort(
    val metadata: CohortMetadata,
    val eligibility: List<Eligibility>
)
