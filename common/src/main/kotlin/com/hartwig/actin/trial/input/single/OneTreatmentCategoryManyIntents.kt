package com.hartwig.actin.trial.input.single

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

data class OneTreatmentCategoryManyIntents(
    val category: TreatmentCategory,
    val intents: Set<Intent>,
)
