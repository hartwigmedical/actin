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
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.logging.log4j.LogManager
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.time.LocalDate

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
        val gsonBuilder = GsonSerializer.createBuilder()
        val gson =
            gsonBuilder.registerTypeHierarchyAdapter(EvaluationMessage::class.java, EvaluationMessageAdapter(gsonBuilder.create())).create()
        return gson.toJson(match)
    }

    fun fromJson(json: String): TreatmentMatch {
        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder
            .registerTypeAdapter(EligibilityFunction::class.java, EligibilityFunctionDeserializer())
            .registerTypeAdapter(LocalDate::class.java, GsonLocalDateAdapter())
            .registerTypeAdapter(Treatment::class.java, TreatmentAdapter())
            .registerTypeAdapter(EvaluationMessage::class.java, EvaluationMessageAdapter(gsonBuilder.create()))
            .create()
        return gson.fromJson(json, TreatmentMatch::class.java)
    }
}