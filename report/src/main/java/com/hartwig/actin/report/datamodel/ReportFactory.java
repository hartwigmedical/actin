package com.hartwig.actin.report.datamodel;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class ReportFactory {

    private static final Logger LOGGER = LogManager.getLogger(ReportFactory.class);

    private ReportFactory() {
    }

    @NotNull
    public static Report fromInputs(@NotNull ClinicalRecord clinical, @NotNull MolecularRecord molecular,
            @NotNull TreatmentMatch treatmentMatch) {
        if (!clinical.patientId().equals(treatmentMatch.sampleId())) {
            LOGGER.warn("Clinical sampleId '{}' not the same as treatment match sampleId '{}'! Using clinical sampleId",
                    clinical.patientId(),
                    treatmentMatch.sampleId());
        }

        return ImmutableReport.builder()
                .sampleId(clinical.patientId())
                .clinical(clinical)
                .molecular(molecular)
                .treatmentMatch(treatmentMatch)
                .build();
    }
}
