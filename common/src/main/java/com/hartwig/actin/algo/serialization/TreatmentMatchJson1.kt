package com.hartwig.actin.algo.serialization

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableCohortMatch
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.CriterionReference
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification
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
import java.io.IOException
import java.lang.reflect.Type
import java.nio.file.Files

object TreatmentMatchJson {
    private val LOGGER = LogManager.getLogger(TreatmentMatchJson::class.java)
    private const val TREATMENT_MATCH_EXTENSION = ".treatment_match.json"

    @Throws(IOException::class)
    fun write(match: TreatmentMatch, directory: String) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val jsonFile = path + match.patientId() + TREATMENT_MATCH_EXTENSION
        LOGGER.info("Writing patient treatment match to {}", jsonFile)
        val writer = BufferedWriter(FileWriter(jsonFile))
        writer.write(toJson(match))
        writer.close()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(treatmentMatchJson: String): TreatmentMatch {
        return fromJson(Files.readString(File(treatmentMatchJson).toPath()))
    }

    @JvmStatic
    @VisibleForTesting
    fun toJson(match: TreatmentMatch): String {
        return GsonSerializer.create().toJson(match)
    }

    @JvmStatic
    @VisibleForTesting
    fun fromJson(json: String): TreatmentMatch {
        val gson = GsonBuilder().registerTypeAdapter(TreatmentMatch::class.java, TreatmentMatchCreator()).create()
        return gson.fromJson(json, TreatmentMatch::class.java)
    }

    private class TreatmentMatchCreator : JsonDeserializer<TreatmentMatch> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            jsonElement: JsonElement, type: Type,
            jsonDeserializationContext: JsonDeserializationContext
        ): TreatmentMatch {
            val match = jsonElement.asJsonObject
            return ImmutableTreatmentMatch.builder()
                .patientId(Json.string(match, "patientId"))
                .sampleId(Json.string(match, "sampleId"))
                .referenceDate(Json.date(match, "referenceDate"))
                .referenceDateIsLive(Json.bool(match, "referenceDateIsLive"))
                .trialMatches(toTrialMatches(Json.array(match, "trialMatches")))
                .build()
        }

        companion object {
            private fun toTrialMatches(trialMatches: JsonArray): List<TrialMatch> {
                val trialEligibilities: MutableList<TrialMatch> = Lists.newArrayList()
                for (element in trialMatches) {
                    trialEligibilities.add(toTrialMatch(element.asJsonObject))
                }
                return trialEligibilities
            }

            private fun toTrialMatch(`object`: JsonObject): TrialMatch {
                return ImmutableTrialMatch.builder()
                    .identification(toIdentification(Json.`object`(`object`, "identification")))
                    .isPotentiallyEligible(Json.bool(`object`, "isPotentiallyEligible"))
                    .evaluations(toEvaluations(`object`["evaluations"]))
                    .cohorts(toCohorts(Json.array(`object`, "cohorts")))
                    .build()
            }

            private fun toIdentification(identification: JsonObject): TrialIdentification {
                return ImmutableTrialIdentification.builder()
                    .trialId(Json.string(identification, "trialId"))
                    .open(Json.bool(identification, "open"))
                    .acronym(Json.string(identification, "acronym"))
                    .title(Json.string(identification, "title"))
                    .build()
            }

            private fun toCohorts(cohorts: JsonArray): List<CohortMatch> {
                val cohortEligibilities: MutableList<CohortMatch> = Lists.newArrayList()
                for (element in cohorts) {
                    val cohort = element.asJsonObject
                    cohortEligibilities.add(
                        ImmutableCohortMatch.builder()
                            .metadata(toMetadata(Json.`object`(cohort, "metadata")))
                            .isPotentiallyEligible(Json.bool(cohort, "isPotentiallyEligible"))
                            .evaluations(toEvaluations(cohort["evaluations"]))
                            .build()
                    )
                }
                return cohortEligibilities
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

            private fun toEvaluations(evaluations: JsonElement): Map<Eligibility, Evaluation> {
                val map: MutableMap<Eligibility, Evaluation> = Maps.newTreeMap(EligibilityComparator())
                if (evaluations.isJsonArray) {
                    for (element in evaluations.asJsonArray) {
                        val array = element.asJsonArray
                        map[toEligibility(array[0].asJsonObject)] = toEvaluation(
                            array[1].asJsonObject
                        )
                    }
                }
                return map
            }

            private fun toEligibility(eligibility: JsonObject): Eligibility {
                return ImmutableEligibility.builder()
                    .references(toReferences(Json.array(eligibility, "references")))
                    .function(toEligibilityFunction(Json.`object`(eligibility, "function")))
                    .build()
            }

            private fun toEvaluation(evaluation: JsonObject): Evaluation {
                return ImmutableEvaluation.builder()
                    .result(EvaluationResult.valueOf(Json.string(evaluation, "result")))
                    .recoverable(Json.bool(evaluation, "recoverable"))
                    .inclusionMolecularEvents(Json.stringList(evaluation, "inclusionMolecularEvents"))
                    .exclusionMolecularEvents(Json.stringList(evaluation, "exclusionMolecularEvents"))
                    .passSpecificMessages(Json.stringList(evaluation, "passSpecificMessages"))
                    .passGeneralMessages(Json.stringList(evaluation, "passGeneralMessages"))
                    .warnSpecificMessages(Json.stringList(evaluation, "warnSpecificMessages"))
                    .warnGeneralMessages(Json.stringList(evaluation, "warnGeneralMessages"))
                    .undeterminedSpecificMessages(Json.stringList(evaluation, "undeterminedSpecificMessages"))
                    .undeterminedGeneralMessages(Json.stringList(evaluation, "undeterminedGeneralMessages"))
                    .failSpecificMessages(Json.stringList(evaluation, "failSpecificMessages"))
                    .failGeneralMessages(Json.stringList(evaluation, "failGeneralMessages"))
                    .build()
            }

            private fun toReferences(referenceArray: JsonArray): Set<CriterionReference> {
                val references: MutableSet<CriterionReference> = Sets.newTreeSet(CriterionReferenceComparator())
                for (element in referenceArray) {
                    val obj = element.asJsonObject
                    references.add(ImmutableCriterionReference.builder().id(Json.string(obj, "id")).text(Json.string(obj, "text")).build())
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
                for (element in parameterArray) {
                    if (element.isJsonObject) {
                        parameters.add(toEligibilityFunction(element.asJsonObject))
                    } else if (element.isJsonPrimitive) {
                        parameters.add(element.asJsonPrimitive.asString)
                    }
                }
                return parameters
            }
        }
    }
}