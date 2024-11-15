package com.hartwig.actin.datamodel.clinical.treatment

import java.lang.reflect.Type

enum class TreatmentClass(val treatmentClass: Type) {
    DRUG_TREATMENT(DrugTreatment::class.java),
    OTHER_TREATMENT(OtherTreatment::class.java),
    RADIOTHERAPY(Radiotherapy::class.java),
    NONE(OtherTreatment::class.java);
}