package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialPhase

data class Cohort(
    val trialId: String,
    val acronym: String,
    val name: String?,
    val molecularEvents: Set<String> = emptySet(),
    val isPotentiallyEligible: Boolean = false,
    val isOpen: Boolean,
    val hasSlotsAvailable: Boolean,
    val ignore: Boolean = false,
    val warnings: Set<String> = emptySet(),
    val fails: Set<String> = emptySet(),
    val phase: TrialPhase? = null
)