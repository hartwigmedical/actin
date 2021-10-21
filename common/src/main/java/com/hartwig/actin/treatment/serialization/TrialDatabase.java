package com.hartwig.actin.treatment.serialization;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;

public final class TrialDatabase {

    private TrialDatabase() {
    }

    @NotNull
    public static List<Trial> readFromDir(@NotNull String trialDirectory) {
        return Lists.newArrayList();
    }
}
