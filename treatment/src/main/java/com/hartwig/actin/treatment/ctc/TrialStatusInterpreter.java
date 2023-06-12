package com.hartwig.actin.treatment.ctc;

import java.util.List;

import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TrialStatusInterpreter {

    @Nullable
    public static Boolean interpret(@NotNull List<CTCDatabaseEntry> entries, @NotNull TrialDefinitionConfig trialConfig) {
        return true;
    }
}
