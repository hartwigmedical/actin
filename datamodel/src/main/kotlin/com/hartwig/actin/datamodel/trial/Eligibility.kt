package com.hartwig.actin.datamodel.trial

data class Eligibility(
    val references: Set<CriterionReference>,
    val function: EligibilityFunction
)
