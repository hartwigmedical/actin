package com.hartwig.actin.trial.input.single

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

data class OneSpecificTreatmentOneTreatmentCategoryManyTypes(
    val treatment: Treatment,
    val category: TreatmentCategory,
    val types: Set<TreatmentType>?
)
