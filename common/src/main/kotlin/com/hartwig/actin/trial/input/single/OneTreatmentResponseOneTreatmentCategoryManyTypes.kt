package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse

data class OneTreatmentResponseOneTreatmentCategoryManyTypes(
    val treatmentResponse: TreatmentResponse,
    val category: TreatmentCategory,
    val types: Set<TreatmentType>
)