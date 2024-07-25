package com.hartwig.actin.database.historic.serialization

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
import com.hartwig.actin.trial.datamodel.CriterionReference
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
        val treatmentMatch = JsonParser.parseReader(reader).asJsonObject

        val treatmentMatchRecord = TreatmentMatch(
            patientId = extractPatientId(treatmentMatch),
            sampleId = Json.string(treatmentMatch, "sampleId"),
            trialSource = "",
            referenceDate = Json.date(treatmentMatch, "referenceDate"),
            referenceDateIsLive = Json.bool(treatmentMatch, "referenceDateIsLive"),
            trialMatches = Json.array(treatmentMatch, "trialMatches").mapNotNull { extractTrialMatch(it) },
            standardOfCareMatches = null,
            personalizedDataAnalysis = null
        )

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main molecular JSON object!", treatmentMatchJson)
        }

        return treatmentMatchRecord
    }

    private fun extractPatientId(treatmentMatch: JsonObject): String {
        return Json.string(treatmentMatch, "sampleId").substring(0, 12)
    }

    private fun extractTrialMatch(trialMatchElement: JsonElement): TrialMatch {
        val trialMatch = trialMatchElement.asJsonObject

        return TrialMatch(
            identification = extractIdentification(Json.`object`(trialMatch, "identification")),
            isPotentiallyEligible = Json.bool(trialMatch, "isPotentiallyEligible"),
            evaluations = extractEvaluations(trialMatch.get("evaluations")),
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

    private fun extractEvaluations(evaluation: JsonElement): Map<Eligibility, Evaluation> {
        val evaluationMap = HashMap<Eligibility, Evaluation>()
        // If there are no evaluations, evaluation becomes a JSON object.
        if (evaluation.isJsonArray) {
            for (evaluationElement in evaluation.asJsonArray) {
                val singleEvaluationArray = evaluationElement.asJsonArray
                evaluationMap[extractEligibility(singleEvaluationArray.get(0).asJsonObject)] =
                    extractEvaluation(singleEvaluationArray.get(1).asJsonObject)
            }
        }
        return evaluationMap
    }

    private fun extractEligibility(eligibility: JsonObject): Eligibility {
        return Eligibility(
            references = HashSet(Json.array(eligibility, "references").mapNotNull { extractReference(it) }),
            function = extractFunction(Json.`object`(eligibility, "function"))
        )
    }

    private fun extractReference(referenceElement: JsonElement): CriterionReference {
        val reference = referenceElement.asJsonObject
        return CriterionReference(
            id = Json.string(reference, "id"),
            text = Json.string(reference, "text")
        )
    }

    private fun extractFunction(function: JsonObject): EligibilityFunction {
        return EligibilityFunction(
            rule = mapRule(Json.string(function, "rule")),
            parameters = Json.array(function, "parameters").mapNotNull { extractParameter(it) })
    }

    private fun mapRule(ruleString: String): EligibilityRule {
        for (rule in EligibilityRule.entries) {
            if (rule.toString() == ruleString) {
                return rule
            }
        }
        LOGGER.warn("  Could not map eligibility rule '{}'", ruleString)
        return EligibilityRule.NOT
    }

    private fun extractParameter(parameterElement: JsonElement): Any {
        return if (parameterElement.isJsonObject) {
            extractFunction(parameterElement.asJsonObject)
        } else if (parameterElement.isJsonPrimitive) {
            parameterElement.asString
        } else {
            throw IllegalStateException("Parameter element is neither an object nor a primitive: $parameterElement")
        }
    }

    private fun extractEvaluation(evaluation: JsonObject): Evaluation {
        return Evaluation(
            result = EvaluationResult.valueOf(Json.string(evaluation, "result")),
            recoverable = Json.bool(evaluation, "recoverable"),
            inclusionMolecularEvents = setOf(),
            exclusionMolecularEvents = setOf(),
            passSpecificMessages = HashSet(Json.stringList(evaluation, "passSpecificMessages")),
            passGeneralMessages = HashSet(Json.stringList(evaluation, "passGeneralMessages")),
            warnSpecificMessages = HashSet(Json.stringList(evaluation, "warnSpecificMessages")),
            warnGeneralMessages = HashSet(Json.stringList(evaluation, "warnGeneralMessages")),
            undeterminedSpecificMessages = HashSet(Json.stringList(evaluation, "undeterminedSpecificMessages")),
            undeterminedGeneralMessages = HashSet(Json.stringList(evaluation, "undeterminedGeneralMessages")),
            failSpecificMessages = HashSet(Json.stringList(evaluation, "failSpecificMessages")),
            failGeneralMessages = HashSet(Json.stringList(evaluation, "failGeneralMessages"))
        )
    }

    private fun extractCohortMatch(cohortMatchElement: JsonElement): CohortMatch {
        val cohortMatch = cohortMatchElement.asJsonObject

        return CohortMatch(
            metadata = extractCohortMetadata(Json.`object`(cohortMatch, "metadata")),
            isPotentiallyEligible = Json.bool(cohortMatch, "isPotentiallyEligible"),
            evaluations = extractEvaluations(cohortMatch.get("evaluations")),
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