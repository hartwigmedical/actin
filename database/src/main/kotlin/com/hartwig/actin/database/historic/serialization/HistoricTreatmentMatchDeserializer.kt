package com.hartwig.actin.database.historic.serialization

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader
import java.time.LocalDate

object HistoricTreatmentMatchDeserializer {

    private val LOGGER: Logger = LogManager.getLogger(HistoricTreatmentMatchDeserializer::class.java)

    fun deserialize(treatmentMatchJson: File): TreatmentMatch {
        val reader = JsonReader(FileReader(treatmentMatchJson))
        val treatmentMatchObject: JsonObject = JsonParser.parseReader(reader).asJsonObject

        val treatmentMatch = TreatmentMatch(
            patientId = "",
            sampleId = "",
            trialSource = "",
            referenceDate = LocalDate.of(1, 1, 1),
            referenceDateIsLive = true,
            trialMatches = listOf(),
            standardOfCareMatches = null,
            personalizedDataAnalysis = null
        )

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main molecular JSON object!", treatmentMatchJson)
        }

        return treatmentMatch
    }
}