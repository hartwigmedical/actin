package com.hartwig.actin.trial.serialization

import com.google.gson.GsonBuilder
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.trial.sort.CriterionReferenceComparator
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.CriterionReferenceDeserializer
import com.hartwig.actin.util.json.CriterionReferenceDeserializer.Companion.toJsonReferenceText
import com.hartwig.actin.util.json.EligibilityFunctionDeserializer
import com.hartwig.actin.util.json.GsonSerializer
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object TrialJson {

    private val LOGGER: Logger = LogManager.getLogger(TrialJson::class.java)
    private const val TRIAL_JSON_EXTENSION: String = ".trial.json"

    fun write(trials: List<Trial>, directory: String) {
        val path: String = Paths.forceTrailingFileSeparator(directory)
        for (trial: Trial in trials) {
            val jsonFile: String = path + trialFileId(trial.identification.nctId) + TRIAL_JSON_EXTENSION
            LOGGER.info(" Writing '{} ({})' to {}", trial.identification.nctId, trial.identification.acronym, jsonFile)
            val writer = BufferedWriter(FileWriter(jsonFile))
            writer.write(toJson(reformatTrial(trial)))
            writer.close()
        }
    }

    fun trialFileId(trialId: String): String {
        return trialId.replace(" ".toRegex(), "_")
    }

    private fun reformatTrial(trial: Trial): Trial {
        val reformattedCohorts = trial.cohorts.map { it.copy(eligibility = reformatEligibilities(it.eligibility)) }
        return trial.copy(
            cohorts = reformattedCohorts,
            generalEligibility = reformatEligibilities(trial.generalEligibility)
        )
    }

    private fun reformatEligibilities(eligibilities: List<Eligibility>): List<Eligibility> {
        return eligibilities.map { eligibility ->
            eligibility.copy(references = eligibility.references.map { reference ->
                reference.copy(text = toJsonReferenceText(reference.text))
            }.toSortedSet(CriterionReferenceComparator()))
        }
    }

    fun readFromDir(directory: String): List<Trial> {
        val files = File(directory).listFiles() ?: throw IllegalArgumentException("Could not retrieve files from $directory")
        return files.filter { it.getName().endsWith(TRIAL_JSON_EXTENSION) }.map(::fromJsonFile)
    }

    fun toJson(trial: Trial): String {
        return GsonSerializer.create().toJson(trial)
    }

    fun fromJson(json: String): Trial {
        val gson = GsonBuilder()
            .registerTypeAdapter(EligibilityFunction::class.java, EligibilityFunctionDeserializer())
            .registerTypeAdapter(CriterionReference::class.java, CriterionReferenceDeserializer())
            .create()
        return gson.fromJson(json, Trial::class.java)
    }

    private fun fromJsonFile(file: File): Trial {
        try {
            return fromJson(Files.readString(file.toPath()))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
