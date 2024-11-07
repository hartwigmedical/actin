package com.hartwig.actin.datamodel.clinical.treatment

import com.hartwig.actin.datamodel.Displayable

enum class TreatmentCategory : Displayable {
    CHEMOTHERAPY,
    TARGETED_THERAPY,
    IMMUNOTHERAPY,
    HORMONE_THERAPY,
    SURGERY,
    TRANSPLANTATION,
    RADIOTHERAPY,
    ABLATION,
    SUPPORTIVE_TREATMENT,
    NONE;

    override fun display(): String {
        return this.toString().replace("_".toRegex(), " ").lowercase()
    }

    companion object {
        val SYSTEMIC_CANCER_TREATMENT_CATEGORIES: Set<TreatmentCategory> =
            setOf(CHEMOTHERAPY, TARGETED_THERAPY, IMMUNOTHERAPY, HORMONE_THERAPY)
    }
}
