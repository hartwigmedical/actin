package com.hartwig.actin.trial.input.single

import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

data class OneTreatmentCategoryManyTypesManyDrugs(
    val category: TreatmentCategory,
    val types: Set<TreatmentType>,
    val drugs: Set<Drug>
)
