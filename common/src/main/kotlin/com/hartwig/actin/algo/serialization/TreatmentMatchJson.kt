package com.hartwig.actin.algo.serialization

import com.google.gson.GsonBuilder
import com.hartwig.actin.clinical.serialization.TreatmentAdapter
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.EligibilityFunctionDeserializer
import com.hartwig.actin.util.json.EvaluationMessageAdapter
import com.hartwig.actin.util.json.GsonLocalDateAdapter
import com.hartwig.actin.util.json.GsonLocalDateTimeAdapter
import com.hartwig.actin.util.json.GsonSetAdapter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime
import org.apache.logging.log4j.LogManager

object TreatmentMatchJson {

    private val logger = LogManager.getLogger(TreatmentMatchJson::class.java)
    private const val TREATMENT_MATCH_EXTENSION = ".treatment_match.json"

    fun write(match: TreatmentMatch, directory: String) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val jsonFile = path + match.patientId + TREATMENT_MATCH_EXTENSION

        logger.info("Writing patient treatment match to {}", jsonFile)
        val writer = BufferedWriter(FileWriter(jsonFile))
        writer.write(toJson(match))
        writer.close()
    }

    fun read(treatmentMatchJson: String): TreatmentMatch {
        return fromJson(Files.readString(File(treatmentMatchJson).toPath()))
    }

    fun toJson(match: TreatmentMatch): String {
        return gsonBuilder().toJson(match)
    }

    fun fromJson(json: String): TreatmentMatch {
        return gsonBuilder().fromJson(json, TreatmentMatch::class.java)
    }

    private fun gsonBuilder() = GsonBuilder().serializeNulls()
        .enableComplexMapKeySerialization()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapter(LocalDateTime::class.java, GsonLocalDateTimeAdapter())
        .registerTypeAdapter(LocalDate::class.java, GsonLocalDateAdapter())
        .registerTypeHierarchyAdapter(Set::class.java, GsonSetAdapter<Any>())
        .registerTypeAdapter(Treatment::class.java, TreatmentAdapter())
        .registerTypeAdapter(EvaluationMessage::class.java, EvaluationMessageAdapter())
        .registerTypeHierarchyAdapter(EvaluationMessage::class.java, EvaluationMessageAdapter())
        .registerTypeAdapter(EligibilityFunction::class.java, EligibilityFunctionDeserializer())
        .create()

}