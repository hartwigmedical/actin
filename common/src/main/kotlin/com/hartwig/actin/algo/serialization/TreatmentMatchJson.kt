package com.hartwig.actin.algo.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.hartwig.actin.clinical.serialization.DrugDeserializer
import com.hartwig.actin.clinical.serialization.TreatmentDeserializer
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.trial.serialization.EligibilityFunctionDeserializer
import com.hartwig.actin.trial.serialization.EligibilityFunctionSerializer
import com.hartwig.actin.util.json.ActinObjectMapper
import com.hartwig.actin.util.json.ComplexKeyMapModule
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object TreatmentMatchJson {

    private val logger = KotlinLogging.logger {}
    private const val TREATMENT_MATCH_EXTENSION = ".treatment_match.json"

    fun write(match: TreatmentMatch, directory: String) {
        write(match, Path.of(directory))
    }

    fun write(match: TreatmentMatch, directory: Path) {
        val jsonFile = directory.resolve(match.patientId + TREATMENT_MATCH_EXTENSION)
        logger.info { "Writing patient treatment match to $jsonFile" }
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
        return mapper.writeValueAsString(match)
    }

    fun fromJson(json: String): TreatmentMatch {
        return mapper.readValue(json, TreatmentMatch::class.java)
    }

    private val mapper: ObjectMapper by lazy {
        ActinObjectMapper.create()
            .registerModule(ComplexKeyMapModule())
            .registerModule(
                SimpleModule().apply {
                    addDeserializer(Treatment::class.java, TreatmentDeserializer)
                    addDeserializer(Drug::class.java, DrugDeserializer)
                    addSerializer(EligibilityFunction::class.java, EligibilityFunctionSerializer)
                    addDeserializer(EligibilityFunction::class.java, EligibilityFunctionDeserializer)
                    addSerializer(EvaluationMessage::class.java, EvaluationMessageSerializer)
                    addDeserializer(EvaluationMessage::class.java, EvaluationMessageDeserializer)
                }
            )
    }
}
