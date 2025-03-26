package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.datamodel.trial.TrialTier

data class InterpretedCohort(
    val trialId: String,
    val acronym: String,
    val nctId: String?,
    val title: String,
    val phase: TrialPhase? = null,
    val source: TrialSource? = null,
    val sourceId: String? = null,
    val tier: TrialTier = TrialTier.TIER_I,
    val link: String? = null,
    val locations: List<String> = emptyList(),
    val name: String?,
    val isOpen: Boolean,
    val hasSlotsAvailable: Boolean,
    val ignore: Boolean = false,
    val molecularEvents: Set<String> = emptySet(),
    val isPotentiallyEligible: Boolean = false,
    val isMissingMolecularResultForEvaluation: Boolean? = null,
    val warnings: Set<String> = emptySet(),
    val fails: Set<String> = emptySet()
)