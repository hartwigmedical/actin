package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialPhase

data class EvaluatedCohort(
    val trialId: String,
    val acronym: String,
    val cohort: String?,
    val molecularEvents: Set<String>,
    val isPotentiallyEligible: Boolean,
    val isMissingGenesForSufficientEvaluation: Boolean,
    val isOpen: Boolean,
    val hasSlotsAvailable: Boolean,
    val warnings: Set<String>,
    val fails: Set<String>,
    val phase: TrialPhase? = null
)