package com.hartwig.actin.trial.input.single

import com.hartwig.actin.clinical.datamodel.AtcLevel

data class OneMedicationCategory(
    val categoryName: String,
    val atcLevels: Set<AtcLevel>
)
