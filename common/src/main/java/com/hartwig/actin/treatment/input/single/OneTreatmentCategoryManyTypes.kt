package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

data class OneTreatmentCategoryManyTypes(
    val category: TreatmentCategory,
    val types: Set<TreatmentType>
)
