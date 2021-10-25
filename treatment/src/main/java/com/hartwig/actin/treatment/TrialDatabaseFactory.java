package com.hartwig.actin.treatment;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.database.TrialConfigDatabase;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;

public final class TrialDatabaseFactory {

    private TrialDatabaseFactory() {
    }

    @NotNull
    public static List<Trial> create(@NotNull TrialConfigDatabase trialConfigDatabase) {
        List<Trial> trials = Lists.newArrayList();

        return trials;
    }
}
