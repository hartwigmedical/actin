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
        if (!clinical.patientId().equals(molecular.patientId())) {
            LOGGER.warn("Clinical patientId '{}' not the same as molecular patientId '{}'! Using clinical sampleId",
                    clinical.patientId(),
                    molecular.patientId());
        }

        return ImmutablePatientRecord.builder().patientId(clinical.patientId()).clinical(clinical).molecular(molecular).build();
    }
}
