package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.Drug

data class ManyDrugsOneInteger(
    val drugs: Set<Drug>,
    val integer: Int
)