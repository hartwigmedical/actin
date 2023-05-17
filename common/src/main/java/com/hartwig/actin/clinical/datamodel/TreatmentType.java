package com.hartwig.actin.clinical.datamodel;

import java.lang.reflect.Type;

public enum TreatmentType {

    CHEMOTHERAPY(Chemotherapy.class),
    COMBINED_THERAPY(CombinedTherapy.class),
    IMMUNOTHERAPY(Immunotherapy.class),
    OTHER_THERAPY(OtherTherapy.class),
    RADIOTHERAPY(Radiotherapy.class),
    SURGERY(SurgicalTreatment.class),
    TARGETED_THERAPY(TargetedTherapy.class);

    private final Type treatmentClass;

    TreatmentType(final Type treatmentClass) {
        this.treatmentClass = treatmentClass;
    }

    public Type treatmentClass() {
        return treatmentClass;
    }
}
