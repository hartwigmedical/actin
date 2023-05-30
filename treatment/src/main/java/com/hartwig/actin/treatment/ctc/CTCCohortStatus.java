package com.hartwig.actin.treatment.ctc;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public enum CTCCohortStatus {
    OPEN,
    CLOSED;

    private static final Logger LOGGER = LogManager.getLogger(CTCCohortStatus.class);

    private static final Set<String> OPEN_STATES = Set.of("Open");
    private static final Set<String> CLOSED_STATES = Set.of("Gesloten", "Nog niet geopend", "Onbekend", "Tijdelijk gesloten");

    @NotNull
    public static CTCCohortStatus fromCohortStatusString(@NotNull String string) {
        if (OPEN_STATES.stream().anyMatch(state -> state.equalsIgnoreCase(string))) {
            return OPEN;
        } else if (CLOSED_STATES.stream().anyMatch(state -> state.equalsIgnoreCase(string))) {
            return CLOSED;
        }

        LOGGER.warn("Could not interpret cohort status string: '{}'. Assuming status implies CLOSED", string);
        return CLOSED;
    }
}
