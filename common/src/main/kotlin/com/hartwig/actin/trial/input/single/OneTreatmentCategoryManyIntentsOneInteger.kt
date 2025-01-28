package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

data class OneTreatmentCategoryManyIntentsOneInteger(
    val category: TreatmentCategory,
    val intents: Set<Intent>,
    val integer: Int,
)
