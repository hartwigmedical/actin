package com.hartwig.actin.clinical.datamodel.treatment;

import java.lang.reflect.Type;

public enum TreatmentClass {

    DRUG_THERAPY(DrugTherapy.class),
    OTHER_TREATMENT(OtherTreatment.class),
    RADIOTHERAPY(Radiotherapy.class);

    private final Type classType;

    TreatmentClass(Type classType) {
        this.classType = classType;
    }

    public Type treatmentClass() {
        return classType;
    }
}