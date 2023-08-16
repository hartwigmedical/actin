package com.hartwig.actin.algo.soc.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.treatment.datamodel.EligibilityFunction

data class TreatmentCandidate(
    val treatment: Treatment,
    val isOptional: Boolean,
    val expectedBenefitScore: Int,
    val eligibilityFunctions: Set<EligibilityFunction>
)