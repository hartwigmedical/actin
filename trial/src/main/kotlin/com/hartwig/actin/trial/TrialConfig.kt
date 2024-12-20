package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource

data class InclusionCriterion(val inclusionRule: String, val referenceIds: List<InclusionCriterionReference>?)

data class InclusionCriterionReference(val id: String, val text: String)

data class TrialState(
    val trialId: String,
    val source: TrialSource,
    val nctId: String?,
    val open: Boolean,
    val acronym: String,
    val title: String,
    val phase: TrialPhase?,
    val inclusionCriterion: List<InclusionCriterion>,
    val cohorts: List<CohortState>,
    val locations: List<String>
)

data class CohortState(
    val cohortId: String,
    val open: Boolean,
    val slotsAvailable: Boolean,
    val description: String,
    val inclusionCriterion: List<InclusionCriterion>,
)