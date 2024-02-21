package com.hartwig.actin.clinical.datamodel.treatment

import com.hartwig.actin.Displayable
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver

enum class TreatmentCategory : Displayable {
    CHEMOTHERAPY,
    TARGETED_THERAPY,
    IMMUNOTHERAPY,
    HORMONE_THERAPY,
    SURGERY,
    TRANSPLANTATION,
    RADIOTHERAPY,
    ABLATION,
    SUPPORTIVE_TREATMENT;

    override fun display(): String {
        return TreatmentCategoryResolver.toString(this).lowercase()
    }

    companion object {
        val CANCER_TREATMENT_CATEGORIES: Set<TreatmentCategory> =
            setOf(CHEMOTHERAPY, TARGETED_THERAPY, IMMUNOTHERAPY, HORMONE_THERAPY)
    }
}
