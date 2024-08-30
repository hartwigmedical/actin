package com.hartwig.actin.datamodel.clinical.treatment.history

import com.hartwig.actin.datamodel.clinical.treatment.Treatment

data class TreatmentStage(
    val treatment: Treatment,
    val cycles: Int?,
    val startYear: Int?,
    val startMonth: Int?,
)
