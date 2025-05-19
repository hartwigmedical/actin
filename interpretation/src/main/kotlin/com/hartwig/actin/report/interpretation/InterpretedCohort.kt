package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource

data class InterpretedCohort(
    val trialId: String,
    val acronym: String,
    val nctId: String?,
    val title: String,
    val phase: TrialPhase?,
    val source: TrialSource?,
    val sourceId: String?,
    val locations: Set<String>,
    val url: String?,
    val name: String?,
    val isOpen: Boolean,
    val hasSlotsAvailable: Boolean,
    val ignore: Boolean,
    val isEvaluable: Boolean,
    val molecularEvents: Set<String>,
    val isPotentiallyEligible: Boolean,
    val isMissingMolecularResultForEvaluation: Boolean,
    val warnings: Set<String>,
    val fails: Set<String>
)