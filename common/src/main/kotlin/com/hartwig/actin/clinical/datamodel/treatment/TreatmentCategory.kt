package com.hartwig.actin.clinical.datamodel.treatment

import com.hartwig.actin.Displayable
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver

enum class TreatmentCategory : Displayable {
    CHEMOTHERAPY,
    RADIOTHERAPY,
    TARGETED_THERAPY,
    IMMUNOTHERAPY,
    HORMONE_THERAPY,
    ANTIVIRAL_THERAPY,
    SUPPORTIVE_TREATMENT,
    SURGERY,
    TRANSPLANTATION,
    TRIAL,
    CAR_T,
    TCR_T,
    GENE_THERAPY,
    PROPHYLACTIC_TREATMENT,
    ABLATION;

    override fun display(): String {
        return TreatmentCategoryResolver.toString(this).lowercase()
    }
}
