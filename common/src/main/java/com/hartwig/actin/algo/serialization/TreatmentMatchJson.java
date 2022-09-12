package com.hartwig.actin.algo.serialization;

import static com.hartwig.actin.util.json.Json.array;
import static com.hartwig.actin.util.json.Json.bool;
import static com.hartwig.actin.util.json.Json.date;
import static com.hartwig.actin.util.json.Json.object;
import static com.hartwig.actin.util.json.Json.string;
import static com.hartwig.actin.util.json.Json.stringList;

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
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableCohortMatch;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
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
import com.hartwig.actin.treatment.sort.CriterionReferenceComparator;
import com.hartwig.actin.treatment.sort.EligibilityComparator;
import com.hartwig.actin.util.Paths;
import com.hartwig.actin.util.json.GsonSerializer;

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
        String jsonFile = path + match.patientId() + TREATMENT_MATCH_EXTENSION;

        LOGGER.info("Writing patient treatment match to {}", jsonFile);
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
                    .patientId(string(match, "patientId"))
                    .referenceDate(date(match, "referenceDate"))
                    .referenceDateIsLive(bool(match, "referenceDateIsLive"))
                    .trialMatches(toTrialMatches(array(match, "trialMatches")))
                    .build();
        }

        @NotNull
        private static List<TrialMatch> toTrialMatches(@NotNull JsonArray trialMatches) {
            List<TrialMatch> trialEligibilities = Lists.newArrayList();
            for (JsonElement element : trialMatches) {
                trialEligibilities.add(toTrialMatch(element.getAsJsonObject()));
            }
            return trialEligibilities;
        }

        @NotNull
        private static TrialMatch toTrialMatch(@NotNull JsonObject object) {
            return ImmutableTrialMatch.builder()
                    .identification(toIdentification(object(object, "identification")))
                    .isPotentiallyEligible(bool(object, "isPotentiallyEligible"))
                    .evaluations(toEvaluations(object.get("evaluations")))
                    .cohorts(toCohorts(array(object, "cohorts")))
                    .build();
        }

        @NotNull
        private static TrialIdentification toIdentification(@NotNull JsonObject identification) {
            return ImmutableTrialIdentification.builder()
                    .trialId(string(identification, "trialId"))
                    .open(bool(identification, "open"))
                    .acronym(string(identification, "acronym"))
                    .title(string(identification, "title"))
                    .build();
        }

        @NotNull
        private static List<CohortMatch> toCohorts(@NotNull JsonArray cohorts) {
            List<CohortMatch> cohortEligibilities = Lists.newArrayList();
            for (JsonElement element : cohorts) {
                JsonObject cohort = element.getAsJsonObject();

                cohortEligibilities.add(ImmutableCohortMatch.builder()
                        .metadata(toMetadata(object(cohort, "metadata")))
                        .isPotentiallyEligible(bool(cohort, "isPotentiallyEligible"))
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
                    .slotsAvailable(bool(cohort, "slotsAvailable"))
                    .blacklist(bool(cohort, "blacklist"))
                    .description(string(cohort, "description"))
                    .build();
        }

        @NotNull
        private static Map<Eligibility, Evaluation> toEvaluations(@NotNull JsonElement evaluations) {
            Map<Eligibility, Evaluation> map = Maps.newTreeMap(new EligibilityComparator());
            if (evaluations.isJsonArray()) {
                for (JsonElement element : evaluations.getAsJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    map.put(toEligibility(array.get(0).getAsJsonObject()), toEvaluation(array.get(1).getAsJsonObject()));
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
        private static Evaluation toEvaluation(@NotNull JsonObject evaluation) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.valueOf(string(evaluation, "result")))
                    .recoverable(bool(evaluation, "recoverable"))
                    .passSpecificMessages(stringList(evaluation, "passSpecificMessages"))
                    .passGeneralMessages(stringList(evaluation, "passGeneralMessages"))
                    .warnSpecificMessages(stringList(evaluation, "warnSpecificMessages"))
                    .warnGeneralMessages(stringList(evaluation, "warnGeneralMessages"))
                    .undeterminedSpecificMessages(stringList(evaluation, "undeterminedSpecificMessages"))
                    .undeterminedGeneralMessages(stringList(evaluation, "undeterminedGeneralMessages"))
                    .failSpecificMessages(stringList(evaluation, "failSpecificMessages"))
                    .failGeneralMessages(stringList(evaluation, "failGeneralMessages"))
                    .build();
        }

        @NotNull
        private static Set<CriterionReference> toReferences(@NotNull JsonArray referenceArray) {
            Set<CriterionReference> references = Sets.newTreeSet(new CriterionReferenceComparator());
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