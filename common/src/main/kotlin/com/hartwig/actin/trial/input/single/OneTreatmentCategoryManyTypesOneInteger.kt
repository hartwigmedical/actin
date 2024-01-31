package com.hartwig.actin.trial.input.single

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

data class OneTreatmentCategoryManyTypesOneInteger(
    val category: TreatmentCategory,
    val types: Set<TreatmentType>,
    val integer: Int
)
