package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

data class ManyTreatmentCategories(
    val treatmentCategories: Set<TreatmentCategory>
)
