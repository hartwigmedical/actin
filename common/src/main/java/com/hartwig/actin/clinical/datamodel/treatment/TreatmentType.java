package com.hartwig.actin.clinical.datamodel.treatment;

import java.lang.reflect.Type;

public enum TreatmentType {

    DRUG_THERAPY(DrugTherapy.class),
    OTHER_TREATMENT(OtherTreatment.class),
    RADIOTHERAPY(Radiotherapy.class);

    private final Type treatmentClass;

    TreatmentType(Type treatmentClass) {
        this.treatmentClass = treatmentClass;
    }

    public Type treatmentClass() {
        return treatmentClass;
    }
}