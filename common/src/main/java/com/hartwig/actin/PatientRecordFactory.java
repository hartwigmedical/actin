package com.hartwig.actin;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class PatientRecordFactory {

    private static final Logger LOGGER = LogManager.getLogger(PatientRecordFactory.class);

    private PatientRecordFactory() {
    }

    @NotNull
    public static PatientRecord fromInputs(@NotNull ClinicalRecord clinical, @NotNull MolecularRecord molecular) {
        if (!clinical.sampleId().equals(molecular.sampleId())) {
            LOGGER.warn("Clinical sampleId '{}' not the same as molecular sampleId '{}'! Using clinical sampleId",
                    clinical.sampleId(),
                    molecular.sampleId());
        }

        return ImmutablePatientRecord.builder().sampleId(clinical.sampleId()).clinical(clinical).molecular(molecular).build();
    }
}
