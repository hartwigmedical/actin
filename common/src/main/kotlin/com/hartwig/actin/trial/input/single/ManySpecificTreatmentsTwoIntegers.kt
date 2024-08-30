package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.Treatment

data class ManySpecificTreatmentsTwoIntegers(
    val integer1: Int,
    val integer2: Int,
    val treatments: List<Treatment>
)
