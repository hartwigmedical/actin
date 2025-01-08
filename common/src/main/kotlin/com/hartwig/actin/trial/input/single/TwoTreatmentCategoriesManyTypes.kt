package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

data class TwoTreatmentCategoriesManyTypes(
    val category1: TreatmentCategory,
    val types1: Set<TreatmentType>,
    val category2: TreatmentCategory,
    val types2: Set<TreatmentType>
)
