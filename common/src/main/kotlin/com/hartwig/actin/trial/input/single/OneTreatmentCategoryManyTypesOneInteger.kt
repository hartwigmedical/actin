package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

data class OneTreatmentCategoryManyTypesOneInteger(
    val category: TreatmentCategory,
    val types: Set<TreatmentType>,
    val integer: Int
)
