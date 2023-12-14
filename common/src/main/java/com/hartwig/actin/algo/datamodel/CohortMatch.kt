package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.Eligibility

data class CohortMatch(val metadata: CohortMetadata, val isPotentiallyEligible: Boolean, val evaluations: Map<Eligibility, Evaluation>)
