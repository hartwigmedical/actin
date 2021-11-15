package com.hartwig.actin.algo.serialization;

import static com.hartwig.actin.util.Json.array;
import static com.hartwig.actin.util.Json.bool;
import static com.hartwig.actin.util.Json.object;
import static com.hartwig.actin.util.Json.string;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.ImmutableCohortEligibility;
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialEligibility;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.hartwig.actin.util.GsonSerializer;
import com.hartwig.actin.util.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class TreatmentMatchJson {

    private static final Logger LOGGER = LogManager.getLogger(TreatmentMatchJson.class);

    private static final String TREATMENT_MATCH_EXTENSION = ".treatment_match.json";

    private TreatmentMatchJson() {
    }

    public static void write(@NotNull TreatmentMatch match, @NotNull String directory) throws IOException {
        String path = Paths.forceTrailingFileSeparator(directory);
        String jsonFile = path + match.sampleId() + TREATMENT_MATCH_EXTENSION;

        LOGGER.info("Writing sample treatment match to {}", jsonFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile));
        writer.write(toJson(match));
        writer.close();
    }

    @NotNull
    public static TreatmentMatch read(@NotNull String treatmentMatchJson) throws IOException {
        return fromJson(Files.readString(new File(treatmentMatchJson).toPath()));
    }

    @VisibleForTesting
    @NotNull
    static String toJson(@NotNull TreatmentMatch match) {
        return GsonSerializer.create().toJson(match);
    }

    @VisibleForTesting
    @NotNull
    static TreatmentMatch fromJson(@NotNull String json) {
        Gson gson = new GsonBuilder().registerTypeAdapter(TreatmentMatch.class, new TreatmentMatchCreator()).create();
        return gson.fromJson(json, TreatmentMatch.class);
    }

    private static class TreatmentMatchCreator implements JsonDeserializer<TreatmentMatch> {

        @Override
        public TreatmentMatch deserialize(@NotNull JsonElement jsonElement, @NotNull Type type,
                @NotNull JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject match = jsonElement.getAsJsonObject();

            return ImmutableTreatmentMatch.builder()
                    .sampleId(string(match, "sampleId"))
                    .trialMatches(toTrialMatches(array(match, "trialMatches")))
                    .build();
        }

        @NotNull
        private static List<TrialEligibility> toTrialMatches(@NotNull JsonArray trialMatches) {
            List<TrialEligibility> trialEligibilities = Lists.newArrayList();
            for (JsonElement element : trialMatches) {
                trialEligibilities.add(toTrialEligibility(element.getAsJsonObject()));
            }
            return trialEligibilities;
        }

        @NotNull
        private static TrialEligibility toTrialEligibility(@NotNull JsonObject object) {
            return ImmutableTrialEligibility.builder()
                    .identification(toIdentification(object(object, "identification")))
                    .overallEvaluation(Evaluation.valueOf(string(object, "overallEvaluation")))
                    .evaluations(toEvaluations(object.get("evaluations")))
                    .cohorts(toCohorts(array(object, "cohorts")))
                    .build();
        }

        @NotNull
        private static TrialIdentification toIdentification(@NotNull JsonObject identification) {
            return ImmutableTrialIdentification.builder()
                    .trialId(string(identification, "trialId"))
                    .acronym(string(identification, "acronym"))
                    .title(string(identification, "title"))
                    .build();
        }

        @NotNull
        private static List<CohortEligibility> toCohorts(@NotNull JsonArray cohorts) {
            List<CohortEligibility> cohortEligibilities = Lists.newArrayList();
            for (JsonElement element : cohorts) {
                JsonObject cohort = element.getAsJsonObject();

                cohortEligibilities.add(ImmutableCohortEligibility.builder()
                        .metadata(toMetadata(object(cohort, "metadata")))
                        .overallEvaluation(Evaluation.valueOf(string(cohort, "overallEvaluation")))
                        .evaluations(toEvaluations(cohort.get("evaluations")))
                        .build());
            }
            return cohortEligibilities;
        }

        @NotNull
        private static CohortMetadata toMetadata(@NotNull JsonObject cohort) {
            return ImmutableCohortMetadata.builder()
                    .cohortId(string(cohort, "cohortId"))
                    .open(bool(cohort, "open"))
                    .description(string(cohort, "description"))
                    .build();
        }

        @NotNull
        private static Map<Eligibility, Evaluation> toEvaluations(@NotNull JsonElement evaluations) {
            Map<Eligibility, Evaluation> map = Maps.newHashMap();
            if (evaluations.isJsonArray()) {
                for (JsonElement element : evaluations.getAsJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    map.put(toEligibility(array.get(0).getAsJsonObject()), Evaluation.valueOf(array.get(1).getAsString()));
                }
            }
            return map;
        }

        @NotNull
        private static Eligibility toEligibility(@NotNull JsonObject eligibility) {
            return ImmutableEligibility.builder()
                    .references(toReferences(array(eligibility, "references")))
                    .function(toEligibilityFunction(object(eligibility, "function")))
                    .build();
        }

        @NotNull
        private static Set<CriterionReference> toReferences(@NotNull JsonArray referenceArray) {
            Set<CriterionReference> references = Sets.newHashSet();
            for (JsonElement element : referenceArray) {
                JsonObject obj = element.getAsJsonObject();
                references.add(ImmutableCriterionReference.builder().id(string(obj, "id")).text(string(obj, "text")).build());
            }
            return references;
        }

        @NotNull
        private static EligibilityFunction toEligibilityFunction(@NotNull JsonObject function) {
            return ImmutableEligibilityFunction.builder()
                    .rule(EligibilityRule.valueOf(string(function, "rule")))
                    .parameters(toParameters(array(function, "parameters")))
                    .build();
        }

        @NotNull
        private static List<Object> toParameters(@NotNull JsonArray parameterArray) {
            List<Object> parameters = Lists.newArrayList();
            for (JsonElement element : parameterArray) {
                if (element.isJsonObject()) {
                    parameters.add(toEligibilityFunction(element.getAsJsonObject()));
                } else if (element.isJsonPrimitive()) {
                    parameters.add(element.getAsJsonPrimitive().getAsString());
                }
            }
            return parameters;
        }
    }
}