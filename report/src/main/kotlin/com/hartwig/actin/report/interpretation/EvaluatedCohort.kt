package com.hartwig.actin.report.interpretation

data class EvaluatedCohort(
    val trialId: String,
    val acronym: String,
    val cohort: String?,
    val molecularEvents: Set<String>,
    val isPotentiallyEligible: Boolean,
    val isBlacklisted: Boolean,
    val isOpen: Boolean,
    val hasSlotsAvailable: Boolean,
    val warnings: Set<String>,
    val fails: Set<String>
)