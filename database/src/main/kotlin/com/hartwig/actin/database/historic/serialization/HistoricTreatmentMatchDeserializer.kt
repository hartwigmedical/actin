package com.hartwig.actin.database.historic.serialization

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.datamodel.Eligibility
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
            evaluations = korneelFixIt(trialMatch),
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

    private fun korneelFixIt(trialMatch: JsonObject): Map<Eligibility, Evaluation> {
        return mapOf()

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