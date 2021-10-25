package com.hartwig.actin.treatment.serialization;

import static com.hartwig.actin.util.Json.array;
import static com.hartwig.actin.util.Json.bool;
import static com.hartwig.actin.util.Json.string;
import static com.hartwig.actin.util.Json.stringList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableCohort;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.datamodel.ImmutableTrial;
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

    public static void write(@NotNull List<Trial> trials, @NotNull String directory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(directory);
        for (Trial trial : trials) {
            String jsonFile = path + trial.trialId().replaceAll(" ", "_") + TRIAL_JSON_EXTENSION;

            if (!Files.exists(new File(jsonFile).toPath())) {
                LOGGER.info(" Writing '{}' to {}", trial.trialId(), jsonFile);
                BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
                writer.write(toJson(trial));
                writer.close();
            } else {
                LOGGER.warn("Cannot write trial {} as JSON file already exists: {}", trial.trialId(), jsonFile);
            }
        }
    }

    @NotNull
    public static List<Trial> readFromDir(@NotNull String directory) throws IOException {
        List<Trial> trials = Lists.newArrayList();
        File[] files = new File(directory).listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Could not retrieve files from " + directory);
        }

        for (File file : files) {
            trials.add(fromJson(Files.readString(file.toPath())));
        }

        return trials;
    }

    @VisibleForTesting
    @NotNull
    static String toJson(@NotNull Trial trial) {
        return new GsonBuilder().serializeNulls().create().toJson(trial);
    }

    @VisibleForTesting
    @NotNull
    static Trial fromJson(@NotNull String json) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Trial.class, new TrialJson.TrialCreator()).create();
        return gson.fromJson(json, Trial.class);
    }

    private static class TrialCreator implements JsonDeserializer<Trial> {

        @Override
        public Trial deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject trial = jsonElement.getAsJsonObject();

            return ImmutableTrial.builder()
                    .trialId(string(trial, "trialId"))
                    .acronym(string(trial, "acronym"))
                    .title(string(trial, "title"))
                    .generalEligibilityFunctions(toEligibilityFunctions(array(trial, "generalEligibilityFunctions")))
                    .cohorts(toCohorts(array(trial, "cohorts")))
                    .build();
        }

        @NotNull
        private static List<EligibilityFunction> toEligibilityFunctions(@NotNull JsonArray eligibilityFunctionArray) {
            List<EligibilityFunction> eligibilityFunctions = Lists.newArrayList();
            for (JsonElement element : eligibilityFunctionArray) {
                JsonObject eligibilityFunction = element.getAsJsonObject();
                eligibilityFunctions.add(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.valueOf(string(eligibilityFunction, "rule")))
                        .parameters(stringList(eligibilityFunction, "parameters"))
                        .build());
            }
            return eligibilityFunctions;
        }

        @NotNull
        private static List<Cohort> toCohorts(@NotNull JsonArray cohortArray) {
            List<Cohort> cohorts = Lists.newArrayList();
            for (JsonElement element : cohortArray) {
                JsonObject cohort = element.getAsJsonObject();
                cohorts.add(ImmutableCohort.builder()
                        .cohortId(string(cohort, "cohortId"))
                        .open(bool(cohort, "open"))
                        .description(string(cohort, "description"))
                        .eligibilityFunctions(toEligibilityFunctions(array(cohort, "eligibilityFunctions")))
                        .build());
            }
            return cohorts;
        }
    }
}
