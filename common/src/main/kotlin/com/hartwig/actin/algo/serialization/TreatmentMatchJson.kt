package com.hartwig.actin.algo.serialization

import com.google.gson.Gson
import com.hartwig.actin.clinical.serialization.TreatmentAdapter
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.util.json.EligibilityFunctionDeserializer
import com.hartwig.actin.util.json.EvaluationMessageAdapter
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object TreatmentMatchJson {

    private val logger = LogManager.getLogger(TreatmentMatchJson::class.java)
    private const val TREATMENT_MATCH_EXTENSION = ".treatment_match.json"

    fun write(match: TreatmentMatch, directory: String) {
        write(match, Path.of(directory))
    }

    fun write(match: TreatmentMatch, directory: Path) {
        val jsonFile = directory.resolve(match.patientId + TREATMENT_MATCH_EXTENSION)
        logger.info("Writing patient treatment match to {}", jsonFile)
        val writer = Files.newBufferedWriter(jsonFile)
        writer.write(toJson(match))
        writer.close()
    }

    fun read(treatmentMatchJson: String): TreatmentMatch {
        return read(File(treatmentMatchJson).toPath())
    }

    fun read(treatmentMatchJson: Path): TreatmentMatch {
        return fromJson(Files.readString(treatmentMatchJson))
    }

    fun toJson(match: TreatmentMatch): String {
        return gson().toJson(match)
    }

    fun fromJson(json: String): TreatmentMatch {
        return gson().fromJson(json, TreatmentMatch::class.java)
    }

    private fun gson(): Gson {
        val gsonBuilder = GsonSerializer.createBuilder()
        return gsonBuilder.registerTypeAdapter(Treatment::class.java, TreatmentAdapter())
            .registerTypeAdapter(EvaluationMessage::class.java, EvaluationMessageAdapter(gsonBuilder.create()))
            .registerTypeHierarchyAdapter(EvaluationMessage::class.java, EvaluationMessageAdapter(gsonBuilder.create()))
            .registerTypeAdapter(EligibilityFunction::class.java, EligibilityFunctionDeserializer())
            .create()
    }

}