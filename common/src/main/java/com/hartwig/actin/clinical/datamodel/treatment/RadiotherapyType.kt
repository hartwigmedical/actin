package com.hartwig.actin.clinical.datamodel.treatment

import java.util.*

enum class RadiotherapyType : TreatmentType {
    BRACHYTHERAPY,
    CYBERKNIFE,
    RADIOISOTOPE,
    STEREOTACTIC;

    override fun category(): TreatmentCategory {
        return TreatmentCategory.RADIOTHERAPY
    }

    override fun display(): String {
        return toString().replace("_", " ").lowercase(Locale.getDefault())
    }
}
