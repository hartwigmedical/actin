package com.hartwig.actin.trial.datamodel

data class Eligibility(
    val references: Set<CriterionReference>,
    val function: EligibilityFunction
)
