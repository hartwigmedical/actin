package com.hartwig.actin.treatment.ctc;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TrialStatusInterpreter {

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

        return null;
    }

    @VisibleForTesting
    @NotNull
    static String extractTrialId(@NotNull CTCDatabaseEntry entry) {
        return CTCModel.CTC_TRIAL_PREFIX + " " + entry.studyMETC();
    }
}
