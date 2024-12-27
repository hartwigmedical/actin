package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource

data class InclusionCriterionConfig(val inclusionRule: String, val referenceIds: List<InclusionCriterionReferenceConfig>?)

data class InclusionCriterionReferenceConfig(val id: String, val text: String)

data class TrialConfig(
    val trialId: String,
    val source: TrialSource,
    val nctId: String?,
    val open: Boolean,
    val acronym: String,
    val title: String,
    val phase: TrialPhase?,
    val inclusionCriterion: List<InclusionCriterionConfig>,
    val cohorts: List<CohortConfig>,
    val locations: List<String>
)

data class CohortConfig(
    val cohortId: String,
    val open: Boolean,
    val slotsAvailable: Boolean,
    val description: String,
    val ignore: Boolean,
    val evaluable: Boolean,
    val inclusionCriterion: List<InclusionCriterionConfig>,
)