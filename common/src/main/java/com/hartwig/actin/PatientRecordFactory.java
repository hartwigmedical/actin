package com.hartwig.actin;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.annotations.VisibleForTesting;
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

        if (!clinical.tumor().doids().equals(molecular.configuredPrimaryTumorDoids())) {
            LOGGER.warn("Primary tumor DOIDs not equal between clinical ({}) and molecular ({})!",
                    concat(clinical.tumor().doids()),
                    concat(molecular.configuredPrimaryTumorDoids()));
        }

        return ImmutablePatientRecord.builder().sampleId(clinical.sampleId()).clinical(clinical).molecular(molecular).build();
    }

    @NotNull
    @VisibleForTesting
    static String concat(@NotNull Set<String> strings) {
        if (strings.isEmpty()) {
            return "-";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
