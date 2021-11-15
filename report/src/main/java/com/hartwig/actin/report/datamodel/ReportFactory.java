package com.hartwig.actin.report.datamodel;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class ReportFactory {

    private static final Logger LOGGER = LogManager.getLogger(ReportFactory.class);

    private ReportFactory() {
    }

    @NotNull
    public static Report fromInputs(@NotNull PatientRecord patient, @NotNull TreatmentMatch treatmentMatch) {
        if (!patient.sampleId().equals(treatmentMatch.sampleId())) {
            LOGGER.warn("Patient sampleId '{}' not the same as treatment match sampleId '{}'! Using patient sampleId",
                    patient.sampleId(),
                    treatmentMatch.sampleId());
        }

        return ImmutableReport.builder()
                .sampleId(patient.sampleId())
                .clinical(patient.clinical())
                .molecular(patient.molecular())
                .treatmentMatch(treatmentMatch)
                .build();
    }
}
