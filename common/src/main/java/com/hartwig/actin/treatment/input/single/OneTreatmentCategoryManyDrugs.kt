package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

data class OneTreatmentCategoryManyDrugs(
    val category: TreatmentCategory,
    val drugs: Set<Drug>,
)
