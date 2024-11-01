package com.hartwig.actin.datamodel.algo

import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.TrialIdentification

data class TrialMatch(
    val identification: TrialIdentification,
    val isPotentiallyEligible: Boolean,
    val evaluations: Map<Eligibility, Evaluation>,
    val cohorts: List<CohortMatch>,
    val nonEvaluableCohorts: List<CohortMetadata>
)