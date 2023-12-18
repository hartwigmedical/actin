package com.hartwig.actin.treatment.serialization

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.gson.JsonArray
import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.ImmutableCohort
import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.Files
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

object TrialJson {
    private val LOGGER: Logger = LogManager.getLogger(TrialJson::class.java)
    private val TRIAL_JSON_EXTENSION: String = ".trial.json"
    private val JSON_REFERENCE_TEXT_LINE_BREAK: String = "<enter>"
    private val JAVA_REFERENCE_TEXT_LINE_BREAK: String = "\n"

    @Throws(IOException::class)
    fun write(trials: List<Trial>, directory: String) {
        val path: String = Paths.forceTrailingFileSeparator(directory)
        for (trial: Trial in trials) {
            val jsonFile: String = path + trialFileId(trial.identification().trialId()) + TRIAL_JSON_EXTENSION
            LOGGER.info(" Writing '{} ({})' to {}", trial.identification().trialId(), trial.identification().acronym(), jsonFile)
            val writer: BufferedWriter = BufferedWriter(FileWriter(jsonFile))
            writer.write(toJson(reformatTrial(trial)))
            writer.close()
        }
    }

    fun trialFileId(trialId: String): String {
        return trialId.replace(" ".toRegex(), "_")
    }

    private fun reformatTrial(trial: Trial): Trial {
        val reformattedCohorts: MutableList<Cohort> = Lists.newArrayList()
        for (cohort: Cohort? in trial.cohorts()) {
            reformattedCohorts.add(
                ImmutableCohort.builder().from(cohort).eligibility(reformatEligibilities(cohort!!.eligibility())).build()
            )
        }
        return ImmutableTrial.builder()
            .from(trial)
            .cohorts(reformattedCohorts)
            .generalEligibility(reformatEligibilities(trial.generalEligibility()))
            .build()
    }

    private fun reformatEligibilities(eligibilities: List<Eligibility>): List<Eligibility> {
        val reformattedEligibility: MutableList<Eligibility> = Lists.newArrayList()
        for (eligibility: Eligibility in eligibilities) {
            val reformattedReferences: MutableSet<CriterionReference> = Sets.newTreeSet<CriterionReference>(CriterionReferenceComparator())
            for (reference: CriterionReference? in eligibility.references()) {
                reformattedReferences.add(
                    ImmutableCriterionReference.builder()
                        .from(reference)
                        .text(toJsonReferenceText(reference.text()))
                        .build()
                )
            }
            reformattedEligibility.add(ImmutableEligibility.builder().from(eligibility).references(reformattedReferences).build())
        }
        return reformattedEligibility
    }

    @JvmStatic
    fun readFromDir(directory: String): List<Trial> {
        val files: Array<File>? = File(directory).listFiles()
        if (files == null) {
            throw IllegalArgumentException("Could not retrieve files from " + directory)
        }
        return Arrays.stream<File>(files)
            .filter(Predicate<File>({ file: File -> file.getName().endsWith(TRIAL_JSON_EXTENSION) }))
            .map<Trial>(Function<File, Trial>({ obj: File? -> fromJsonFile() }))
            .collect(Collectors.toList<Trial>())
    }

    @JvmStatic
    @VisibleForTesting
    fun toJson(trial: Trial): String {
        return GsonSerializer.create().toJson(trial)
    }

    @JvmStatic
    @VisibleForTesting
    fun fromJson(json: String): Trial {
        val gson: Gson = GsonBuilder().registerTypeAdapter(Trial::class.java, TrialCreator()).create()
        return gson.fromJson<Trial>(json, Trial::class.java)
    }

    private fun fromJsonFile(file: File): Trial {
        try {
            return fromJson(Files.readString(file.toPath()))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun fromJsonReferenceText(text: String): String {
        return text.replace(JSON_REFERENCE_TEXT_LINE_BREAK.toRegex(), JAVA_REFERENCE_TEXT_LINE_BREAK)
    }

    private fun toJsonReferenceText(text: String): String {
        return text.replace(JAVA_REFERENCE_TEXT_LINE_BREAK.toRegex(), JSON_REFERENCE_TEXT_LINE_BREAK)
    }

    private class TrialCreator() : JsonDeserializer<Trial?> {
        @Throws(JsonParseException::class)
        public override fun deserialize(
            jsonElement: JsonElement, type: Type,
            jsonDeserializationContext: JsonDeserializationContext
        ): Trial {
            val trial: JsonObject = jsonElement.getAsJsonObject()
            return ImmutableTrial.builder()
                .identification(toTrialIdentification(Json.`object`(trial, "identification")))
                .generalEligibility(toEligibility(Json.array(trial, "generalEligibility")))
                .cohorts(toCohorts(Json.array(trial, "cohorts")))
                .build()
        }

        companion object {
            private fun toTrialIdentification(trial: JsonObject): TrialIdentification {
                return ImmutableTrialIdentification.builder()
                    .trialId(Json.string(trial, "trialId"))
                    .open(Json.bool(trial, "open"))
                    .acronym(Json.string(trial, "acronym"))
                    .title(Json.string(trial, "title"))
                    .build()
            }

            private fun toCohorts(cohortArray: JsonArray): List<Cohort> {
                val cohorts: MutableList<Cohort> = Lists.newArrayList()
                for (element: JsonElement in cohortArray) {
                    val cohort: JsonObject = element.getAsJsonObject()
                    cohorts.add(
                        ImmutableCohort.builder()
                            .metadata(toMetadata(Json.`object`(cohort, "metadata")))
                            .eligibility(toEligibility(Json.array(cohort, "eligibility")))
                            .build()
                    )
                }
                return cohorts
            }

            private fun toMetadata(cohort: JsonObject): CohortMetadata {
                return ImmutableCohortMetadata.builder()
                    .cohortId(Json.string(cohort, "cohortId"))
                    .evaluable(Json.bool(cohort, "evaluable"))
                    .open(Json.bool(cohort, "open"))
                    .slotsAvailable(Json.bool(cohort, "slotsAvailable"))
                    .blacklist(Json.bool(cohort, "blacklist"))
                    .description(Json.string(cohort, "description"))
                    .build()
            }

            private fun toEligibility(eligibilityFunctionArray: JsonArray): List<Eligibility> {
                val eligibility: MutableList<Eligibility> = Lists.newArrayList()
                for (element: JsonElement in eligibilityFunctionArray) {
                    val obj: JsonObject = element.getAsJsonObject()
                    eligibility.add(
                        ImmutableEligibility.builder()
                            .references(toReferences(Json.array(obj, "references")))
                            .function(toEligibilityFunction(Json.`object`(obj, "function")))
                            .build()
                    )
                }
                return eligibility
            }

            private fun toReferences(referenceArray: JsonArray): Set<CriterionReference> {
                val references: MutableSet<CriterionReference> = Sets.newTreeSet<CriterionReference>(CriterionReferenceComparator())
                for (element: JsonElement in referenceArray) {
                    val obj: JsonObject = element.getAsJsonObject()
                    references.add(
                        ImmutableCriterionReference.builder()
                            .id(Json.string(obj, "id"))
                            .text(fromJsonReferenceText(Json.string(obj, "text")))
                            .build()
                    )
                }
                return references
            }

            private fun toEligibilityFunction(function: JsonObject): EligibilityFunction {
                return ImmutableEligibilityFunction.builder()
                    .rule(EligibilityRule.valueOf(Json.string(function, "rule")))
                    .parameters(toParameters(Json.array(function, "parameters")))
                    .build()
            }

            private fun toParameters(parameterArray: JsonArray): List<Any> {
                val parameters: MutableList<Any> = Lists.newArrayList()
                for (element: JsonElement in parameterArray) {
                    if (element.isJsonObject()) {
                        parameters.add(toEligibilityFunction(element.getAsJsonObject()))
                    } else if (element.isJsonPrimitive()) {
                        parameters.add(element.getAsJsonPrimitive().getAsString())
                    }
                }
                return parameters
            }
        }
    }
}
