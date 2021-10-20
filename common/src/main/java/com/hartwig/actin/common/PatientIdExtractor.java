package com.hartwig.actin.common;

import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class PatientIdExtractor {

    private static final Logger LOGGER = LogManager.getLogger(PatientIdExtractor.class);

    private PatientIdExtractor() {
    }

    @NotNull
    public static String toPatientId(@NotNull String sampleId) {
        if (sampleId.length() < 12) {
            LOGGER.warn("Cannot extract patientId from sampleId '{}'", sampleId);
            return sampleId;
        }

        StringJoiner joiner = new StringJoiner("-");
        joiner.add(sampleId.substring(0, 4));
        joiner.add(sampleId.substring(4, 6));
        joiner.add(sampleId.substring(6, 8));
        joiner.add(sampleId.substring(8, 12));
        return joiner.toString();
    }
}
