package com.hartwig.actin.treatment.ctc;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TrialStatusInterpreter {

    private static final Logger LOGGER = LogManager.getLogger(TrialStatusInterpreter.class);

    @Nullable
    public static Boolean isOpen(@NotNull List<CTCDatabaseEntry> entries, @NotNull TrialDefinitionConfig trialConfig) {
        boolean isPresentInCTCDatabase = false;
        for (CTCDatabaseEntry entry : entries) {
            String fullTrialId = extractTrialId(entry);
            if (fullTrialId.equalsIgnoreCase(trialConfig.trialId())) {
                isPresentInCTCDatabase = true;
                CTCStatus trialStatus = CTCStatus.fromStatusString(entry.studyStatus());
                if (trialStatus == CTCStatus.OPEN) {
                    return true;
                }
            }
        }

        if (isPresentInCTCDatabase) {
            return false;
        }

        LOGGER.warn("Could not resolve study status for {} ({}) as it does not exist in CTC database",
                trialConfig.trialId(),
                trialConfig.acronym());
        return null;
    }

    @VisibleForTesting
    @NotNull
    static String extractTrialId(@NotNull CTCDatabaseEntry entry) {
        return "MEC " + entry.studyMETC();
    }
}
