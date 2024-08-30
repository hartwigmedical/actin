package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

data class OneTreatmentCategoryManyDrugs(
    val category: TreatmentCategory,
    val drugs: Set<Drug>,
)
