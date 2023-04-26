package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Optional;

import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

final class ProgressiveDiseaseFunctions {

    static final String PD_LABEL = "PD";

    static Optional<Boolean> treatmentResultedInPDOption(PriorTumorTreatment treatment) {
        if (PD_LABEL.equalsIgnoreCase(treatment.stopReason()) || PD_LABEL.equalsIgnoreCase(treatment.bestResponse())) {
            return Optional.of(true);
        } else if (treatment.stopReason() != null && treatment.bestResponse() != null) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
    }
}
