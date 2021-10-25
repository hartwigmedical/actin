package com.hartwig.actin.treatment.serialization;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.util.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class TrialJson {

    private static final Logger LOGGER = LogManager.getLogger(TrialJson.class);

    private static final String TRIAL_JSON_EXTENSION = ".trial.json";

    private TrialJson() {
    }

    public static void write(@NotNull List<Trial> trials, @NotNull String outputDirectory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(outputDirectory);
        for (Trial trial : trials) {
            String jsonFile = path + trial.trialIdWithoutSpace() + TRIAL_JSON_EXTENSION;

            LOGGER.info(" Writing '{}' to {}", trial.trialId(), jsonFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
            writer.write(toJson(trial));
            writer.close();
        }
    }

    @NotNull
    public static List<Trial> readFromDir(@NotNull String treatmentDirectory) {
        return Lists.newArrayList();
    }

    @VisibleForTesting
    @NotNull
    static String toJson(@NotNull Trial trial) {
        return new GsonBuilder().serializeNulls().create().toJson(trial);
    }
}
