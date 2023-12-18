package com.hartwig.actin.algo.serialization

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.CriterionReference
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import com.hartwig.actin.treatment.sort.CriterionReferenceComparator
import com.hartwig.actin.treatment.sort.EligibilityComparator
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.GsonSerializer
import com.hartwig.actin.util.json.Json
import org.apache.logging.log4j.LogManager
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Type
import java.nio.file.Files

object TreatmentMatchJson {
    private val LOGGER = LogManager.getLogger(TreatmentMatchJson::class.java)
    private const val TREATMENT_MATCH_EXTENSION = ".treatment_match.json"

    fun write(match: TreatmentMatch, directory: String) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val jsonFile = path + match.patientId + TREATMENT_MATCH_EXTENSION
        LOGGER.info("Writing patient treatment match to {}", jsonFile)
        val writer = BufferedWriter(FileWriter(jsonFile))
        writer.write(toJson(match))
        writer.close()
    }

    fun read(treatmentMatchJson: String): TreatmentMatch {
        return fromJson(Files.readString(File(treatmentMatchJson).toPath()))
    }

    fun toJson(match: TreatmentMatch): String {
        return GsonSerializer.create().toJson(match)
    }

    fun fromJson(json: String): TreatmentMatch {
        val gson = GsonBuilder().registerTypeAdapter(TreatmentMatch::class.java, TreatmentMatchCreator()).create()
        return gson.fromJson(json, TreatmentMatch::class.java)
    }

    private class TreatmentMatchCreator : JsonDeserializer<TreatmentMatch> {

        override fun deserialize(
            jsonElement: JsonElement, type: Type,
            jsonDeserializationContext: JsonDeserializationContext
        ): TreatmentMatch {
            val match = jsonElement.asJsonObject
            return TreatmentMatch(
                patientId = Json.string(match, "patientId"),
                sampleId = Json.string(match, "sampleId"),
                referenceDate = Json.date(match, "referenceDate"),
                referenceDateIsLive = Json.bool(match, "referenceDateIsLive"),
                trialMatches = toTrialMatches(Json.array(match, "trialMatches"))
            )
        }

        companion object {
            private fun toTrialMatches(trialMatches: JsonArray): List<TrialMatch> {
                return trialMatches.map { toTrialMatch(it.asJsonObject) }
            }

            private fun toTrialMatch(`object`: JsonObject): TrialMatch {
                return TrialMatch(
                    identification = toIdentification(Json.`object`(`object`, "identification")),
                    isPotentiallyEligible = Json.bool(`object`, "isPotentiallyEligible"),
                    evaluations = toEvaluations(`object`["evaluations"]),
                    cohorts = toCohorts(Json.array(`object`, "cohorts"))
                )
            }

            private fun toIdentification(identification: JsonObject): TrialIdentification {
                return TrialIdentification(
                    trialId = Json.string(identification, "trialId"),
                    open = Json.bool(identification, "open"),
                    acronym = Json.string(identification, "acronym"),
                    title = Json.string(identification, "title")
                )
            }

            private fun toCohorts(cohorts: JsonArray): List<CohortMatch> {
                return cohorts.map { element ->
                    val cohort = element.asJsonObject
                    CohortMatch(
                        metadata = toMetadata(Json.`object`(cohort, "metadata")),
                        isPotentiallyEligible = Json.bool(cohort, "isPotentiallyEligible"),
                        evaluations = toEvaluations(cohort["evaluations"])
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

            private fun toEvaluations(evaluations: JsonElement): Map<Eligibility, Evaluation> {
                return if (!evaluations.isJsonArray) emptyMap() else evaluations.asJsonArray.associate { element ->
                    val array = element.asJsonArray
                    toEligibility(array[0].asJsonObject) to toEvaluation(array[1].asJsonObject)
                }
                    .toSortedMap(EligibilityComparator())
            }

            private fun toEligibility(eligibility: JsonObject): Eligibility {
                return Eligibility(
                    references = toReferences(Json.array(eligibility, "references")),
                    function = toEligibilityFunction(Json.`object`(eligibility, "function"))
                )
            }

            private fun toEvaluation(evaluation: JsonObject): Evaluation {
                return Evaluation(
                    result = EvaluationResult.valueOf(Json.string(evaluation, "result")),
                    recoverable = Json.bool(evaluation, "recoverable"),
                    inclusionMolecularEvents = Json.stringSet(evaluation, "inclusionMolecularEvents"),
                    exclusionMolecularEvents = Json.stringSet(evaluation, "exclusionMolecularEvents"),
                    passSpecificMessages = Json.stringSet(evaluation, "passSpecificMessages"),
                    passGeneralMessages = Json.stringSet(evaluation, "passGeneralMessages"),
                    warnSpecificMessages = Json.stringSet(evaluation, "warnSpecificMessages"),
                    warnGeneralMessages = Json.stringSet(evaluation, "warnGeneralMessages"),
                    undeterminedSpecificMessages = Json.stringSet(evaluation, "undeterminedSpecificMessages"),
                    undeterminedGeneralMessages = Json.stringSet(evaluation, "undeterminedGeneralMessages"),
                    failSpecificMessages = Json.stringSet(evaluation, "failSpecificMessages"),
                    failGeneralMessages = Json.stringSet(evaluation, "failGeneralMessages"),
                )
            }

            private fun toReferences(referenceArray: JsonArray): Set<CriterionReference> {
                return referenceArray.map { element ->
                    val obj = element.asJsonObject
                    CriterionReference(
                        id = Json.string(obj, "id"),
                        text = Json.string(obj, "text")
                    )
                }
                    .toSortedSet(CriterionReferenceComparator())
            }

            private fun toEligibilityFunction(function: JsonObject): EligibilityFunction {
                return EligibilityFunction(
                    rule = EligibilityRule.valueOf(Json.string(function, "rule")),
                    parameters = toParameters(Json.array(function, "parameters"))
                )
            }

            private fun toParameters(parameterArray: JsonArray): List<Any> {
                return parameterArray.mapNotNull { element ->
                    when {
                        element.isJsonObject -> {
                            toEligibilityFunction(element.asJsonObject)
                        }

                        element.isJsonPrimitive -> {
                            element.asJsonPrimitive.asString
                        }

                        else -> null
                    }
                }
            }
        }
    }
}