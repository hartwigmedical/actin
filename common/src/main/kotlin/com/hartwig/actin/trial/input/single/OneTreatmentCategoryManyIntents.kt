package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

data class OneTreatmentCategoryManyIntents(
    val category: TreatmentCategory,
    val intents: Set<Intent>,
)
