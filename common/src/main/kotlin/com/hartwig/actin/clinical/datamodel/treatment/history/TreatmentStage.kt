package com.hartwig.actin.clinical.datamodel.treatment.history

import com.hartwig.actin.clinical.datamodel.treatment.Treatment

data class TreatmentStage(
    val treatment: Treatment,
    val cycles: Int?,
    val startYear: Int?,
    val startMonth: Int?,
)
