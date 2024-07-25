package com.hartwig.actin.database.historic.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.TrialIdentification
import com.hartwig.actin.util.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader

object HistoricTreatmentMatchDeserializer {

    private val LOGGER: Logger = LogManager.getLogger(HistoricTreatmentMatchDeserializer::class.java)

    fun deserialize(treatmentMatchJson: File): TreatmentMatch {
        val reader = JsonReader(FileReader(treatmentMatchJson))
        val treatmentMatchObject: JsonObject = JsonParser.parseReader(reader).asJsonObject

        val treatmentMatch = TreatmentMatch(
            patientId = extractPatientId(treatmentMatchObject),
            sampleId = Json.string(treatmentMatchObject, "sampleId"),
            trialSource = "",
            referenceDate = Json.date(treatmentMatchObject, "referenceDate"),
            referenceDateIsLive = Json.bool(treatmentMatchObject, "referenceDateIsLive"),
            trialMatches = Json.array(treatmentMatchObject, "trialMatches").mapNotNull { extractTrialMatch(it) },
            standardOfCareMatches = null,
            personalizedDataAnalysis = null
        )

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main molecular JSON object!", treatmentMatchJson)
        }

        return treatmentMatch
    }

    private fun extractPatientId(treatmentMatch: JsonObject): String {
        val sample: String = Json.string(treatmentMatch, "sampleId")
        return sample.substring(0, 12)
    }

    private fun extractTrialMatch(trialMatchElement: JsonElement): TrialMatch {
        val trialMatch = trialMatchElement.asJsonObject

        return TrialMatch(
            identification = extractIdentification(Json.`object`(trialMatch, "identification")),
            isPotentiallyEligible = Json.bool(trialMatch, "isPotentiallyEligible"),
            evaluations = extractEvaluations(Json.array(trialMatch, "evaluations")),
            cohorts = Json.array(trialMatch, "cohorts").mapNotNull { extractCohortMatch(it) }
        )
    }

    private fun extractIdentification(identification: JsonObject): TrialIdentification {
        return TrialIdentification(
            trialId = Json.string(identification, "trialId"),
            open = Json.bool(identification, "open"),
            acronym = Json.string(identification, "acronym"),
            title = Json.string(identification, "title"),
            nctId = null,
            phase = null
        )
    }

    private fun extractEvaluations(evaluations: JsonArray): Map<Eligibility, Evaluation> {
        val evaluationMap = HashMap<Eligibility, Evaluation>()
        for (evaluationElement in evaluations) {
            val singleEvaluationArray = evaluationElement.asJsonArray
            evaluationMap[extractEligibility(singleEvaluationArray.get(0).asJsonObject)] =
                extractEvaluation(singleEvaluationArray.get(1).asJsonObject)
        }
        return evaluationMap
    }

    private fun extractEligibility(eligibility: JsonObject): Eligibility {
        return Eligibility(references = setOf(), function = EligibilityFunction(EligibilityRule.ACTIVATING_MUTATION_IN_ANY_GENES_X))
    }

    private fun extractEvaluation(evaluation: JsonObject): Evaluation {
        return Evaluation(
            result = EvaluationResult.NOT_EVALUATED,
            recoverable = false
        )
    }

    private fun extractCohortMatch(cohortMatchElement: JsonElement): CohortMatch {
        val cohortMatch = cohortMatchElement.asJsonObject

        return CohortMatch(
            metadata = extractCohortMetadata(Json.`object`(cohortMatch, "metadata")),
            isPotentiallyEligible = false,
            evaluations = mapOf()
        )
    }

    private fun extractCohortMetadata(cohortMetadata: JsonObject): CohortMetadata {
        return CohortMetadata(
            cohortId = Json.string(cohortMetadata, "cohortId"),
            evaluable = true,
            open = Json.bool(cohortMetadata, "open"),
            slotsAvailable = Json.bool(cohortMetadata, "slotsAvailable"),
            blacklist = Json.bool(cohortMetadata, "blacklist"),
            description = Json.string(cohortMetadata, "description")
        )
    }
}