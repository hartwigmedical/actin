package com.hartwig.actin.treatment.datamodel

data class Cohort(val metadata: CohortMetadata, val eligibility: List<Eligibility>)
