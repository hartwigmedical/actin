package com.hartwig.actin.treatment.serialization

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.CriterionReference
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import com.hartwig.actin.treatment.sort.CriterionReferenceComparator
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.GsonSerializer
import com.hartwig.actin.util.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.Files

object TrialJson {
    private val LOGGER: Logger = LogManager.getLogger(TrialJson::class.java)
    private const val TRIAL_JSON_EXTENSION: String = ".trial.json"
    private const val JSON_REFERENCE_TEXT_LINE_BREAK: String = "<enter>"
    private const val JAVA_REFERENCE_TEXT_LINE_BREAK: String = "\n"

    fun write(trials: List<Trial>, directory: String) {
        val path: String = Paths.forceTrailingFileSeparator(directory)
        for (trial: Trial in trials) {
            val jsonFile: String = path + trialFileId(trial.identification.trialId) + TRIAL_JSON_EXTENSION
            LOGGER.info(" Writing '{} ({})' to {}", trial.identification.trialId, trial.identification.acronym, jsonFile)
            val writer = BufferedWriter(FileWriter(jsonFile))
            writer.write(toJson(reformatTrial(trial)))
            writer.close()
        }
    }

    fun trialFileId(trialId: String): String {
        return trialId.replace(" ".toRegex(), "_")
    }

    private fun reformatTrial(trial: Trial): Trial {
        val reformattedCohorts = trial.cohorts.map { it.copy(eligibility = reformatEligibilities(it.eligibility)) }
        return trial.copy(
            cohorts = reformattedCohorts,
            generalEligibility = reformatEligibilities(trial.generalEligibility)
        )
    }

    private fun reformatEligibilities(eligibilities: List<Eligibility>): List<Eligibility> {
        return eligibilities.map { eligibility ->
            eligibility.copy(references = eligibility.references.map { reference ->
                reference.copy(text = toJsonReferenceText(reference.text))
            }.toSortedSet(CriterionReferenceComparator()))
        }
    }

    fun readFromDir(directory: String): List<Trial> {
        val files = File(directory).listFiles() ?: throw IllegalArgumentException("Could not retrieve files from $directory")
        return files.filter { it.getName().endsWith(TRIAL_JSON_EXTENSION) }.map(::fromJsonFile)
    }

    fun toJson(trial: Trial): String {
        return GsonSerializer.create().toJson(trial)
    }

    fun fromJson(json: String): Trial {
        val gson = GsonBuilder().registerTypeAdapter(Trial::class.java, TrialCreator()).create()
        return gson.fromJson(json, Trial::class.java)
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

    private class TrialCreator : JsonDeserializer<Trial> {

        override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): Trial {
            val trial: JsonObject = jsonElement.asJsonObject
            return Trial(
                identification = toTrialIdentification(Json.`object`(trial, "identification")),
                generalEligibility = toEligibility(Json.array(trial, "generalEligibility")),
                cohorts = toCohorts(Json.array(trial, "cohorts"))
            )
        }

        companion object {
            private fun toTrialIdentification(trial: JsonObject): TrialIdentification {
                return TrialIdentification(
                    trialId = Json.string(trial, "trialId"),
                    open = Json.bool(trial, "open"),
                    acronym = Json.string(trial, "acronym"),
                    title = Json.string(trial, "title")
                )
            }

            private fun toCohorts(cohortArray: JsonArray): List<Cohort> {
                return cohortArray.map { element ->
                    val cohort: JsonObject = element.asJsonObject
                    Cohort(
                        metadata = toMetadata(Json.`object`(cohort, "metadata")),
                        eligibility = toEligibility(Json.array(cohort, "eligibility"))
                    )
                }
            }

            private fun toMetadata(cohort: JsonObject): CohortMetadata {
                return CohortMetadata(
                    cohortId = Json.string(cohort, "cohortId"),
                    evaluable = Json.bool(cohort, "evaluable"),
                    open = Json.bool(cohort, "open"),
                    slotsAvailable = Json.bool(cohort, "slotsAvailable"),
                    blacklist = Json.bool(cohort, "blacklist"),
                    description = Json.string(cohort, "description")
                )
            }

            private fun toEligibility(eligibilityFunctionArray: JsonArray): List<Eligibility> {
                return eligibilityFunctionArray.map { element ->
                    val obj: JsonObject = element.asJsonObject
                    Eligibility(
                        references = toReferences(Json.array(obj, "references")),
                        function = toEligibilityFunction(Json.`object`(obj, "function"))
                    )
                }
            }

            private fun toReferences(referenceArray: JsonArray): Set<CriterionReference> {
                return referenceArray.map { element ->
                    val obj: JsonObject = element.asJsonObject
                    CriterionReference(
                        id = Json.string(obj, "id"),
                        text = fromJsonReferenceText(Json.string(obj, "text"))
                    )
                }.toSortedSet(CriterionReferenceComparator())
            }

            private fun toEligibilityFunction(function: JsonObject): EligibilityFunction {
                return EligibilityFunction(
                    rule = EligibilityRule.valueOf(Json.string(function, "rule")),
                    parameters = toParameters(Json.array(function, "parameters"))
                )
            }

            private fun toParameters(parameterArray: JsonArray): List<Any> {
                return parameterArray.mapNotNull { element ->
                    if (element.isJsonObject) {
                        toEligibilityFunction(element.asJsonObject)
                    } else if (element.isJsonPrimitive) {
                        element.asJsonPrimitive.asString
                    } else null
                }
            }
        }
    }
}
