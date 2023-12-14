package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.TrialIdentification

data class TrialMatch(
    val identification: TrialIdentification,
    val isPotentiallyEligible: Boolean,
    val evaluations: Map<Eligibility, Evaluation>,
    val cohorts: List<CohortMatch>
)