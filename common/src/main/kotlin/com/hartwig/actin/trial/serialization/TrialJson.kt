package com.hartwig.actin.trial.serialization

import com.hartwig.actin.clinical.serialization.TreatmentAdapter
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.util.json.EligibilityFunctionDeserializer
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object TrialJson {

    private val logger: Logger = LogManager.getLogger(TrialJson::class.java)
    private const val TRIAL_JSON_EXTENSION: String = ".trial.json"

    fun write(trials: List<Trial>, directory: String) {
        write(trials, Path.of(directory))
    }

    fun write(trials: List<Trial>, directory: Path) {
        for (trial: Trial in trials) {
            val jsonFile = directory.resolve(trialFileId(trial.identification.trialId) + TRIAL_JSON_EXTENSION)
            logger.info(" Writing '{} ({})' to {}", trial.identification.trialId, trial.identification.acronym, jsonFile)
            val writer = Files.newBufferedWriter(jsonFile)
            writer.write(toJson(trial))
            writer.close()
        }
    }

    fun trialFileId(trialId: String): String {
        return trialId.replace("[ /]".toRegex(), "_")
    }

    fun readFromDir(directory: String): List<Trial> {
        return readFromDir(Path.of(directory))
    }

    fun readFromDir(directory: Path): List<Trial> {
        Files.isDirectory(directory) || throw IllegalArgumentException("Not a directory: $directory")
        return Files.list(directory).filter { it.fileName.toString().endsWith(TRIAL_JSON_EXTENSION) }.map(::fromJsonFile).toList()
    }

    fun toJson(trial: Trial): String {
        return gson().toJson(trial)
    }

    fun fromJson(json: String): Trial {
        return gson().fromJson(json, Trial::class.java)
    }

    private fun gson() = GsonSerializer.createBuilder()
        .registerTypeAdapter(Treatment::class.java, TreatmentAdapter())
        .registerTypeAdapter(EligibilityFunction::class.java, EligibilityFunctionDeserializer())
        .create()

    private fun fromJsonFile(file: Path): Trial {
        try {
            return fromJson(Files.readString(file))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
