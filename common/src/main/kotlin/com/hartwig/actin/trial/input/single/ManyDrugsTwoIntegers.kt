package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.Drug

data class ManyDrugsTwoIntegers(
    val drugs: Set<Drug>,
    val integer: Int,
    val halfLife: Int
)