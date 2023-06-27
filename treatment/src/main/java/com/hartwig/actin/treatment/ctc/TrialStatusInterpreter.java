package com.hartwig.actin.treatment.ctc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<CTCStatus> trialStates = entries.stream()
                .filter(entry -> trialConfig.trialId().equalsIgnoreCase(extractTrialId(entry)))
                .map(entry -> CTCStatus.fromStatusString(entry.studyStatus()))
                .collect(Collectors.toSet());

        if (trialStates.size() > 1) {
            LOGGER.warn("Inconsistent study status found for trial '{}' in CTC database. Assuming trial is closed", trialConfig.trialId());
            return false;
        } else if (trialStates.size() == 1) {
            return trialStates.iterator().next() == CTCStatus.OPEN;
        }

        return null;
    }

    @VisibleForTesting
    @NotNull
    static String extractTrialId(@NotNull CTCDatabaseEntry entry) {
        return CTCModel.CTC_TRIAL_PREFIX + " " + entry.studyMETC();
    }
}
