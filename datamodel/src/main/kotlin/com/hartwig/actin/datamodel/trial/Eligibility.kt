package com.hartwig.actin.datamodel.trial

data class Eligibility(
    val references: Set<String>,
    val function: EligibilityFunction
)
