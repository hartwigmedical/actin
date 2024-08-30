package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

data class OneTreatmentCategoryManyTypes(
    val category: TreatmentCategory,
    val types: Set<TreatmentType>
)
