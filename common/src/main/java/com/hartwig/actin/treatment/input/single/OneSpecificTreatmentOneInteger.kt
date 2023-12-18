package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.clinical.datamodel.treatment.Treatment

data class OneSpecificTreatmentOneInteger(
    val integer: Int,
    val treatment: Treatment
)
