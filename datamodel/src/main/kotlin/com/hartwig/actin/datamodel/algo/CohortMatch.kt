package com.hartwig.actin.datamodel.algo

import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility

data class CohortMatch(
    val metadata: CohortMetadata,
    val isPotentiallyEligible: Boolean,
    val evaluations: Map<Eligibility, Evaluation>
)
