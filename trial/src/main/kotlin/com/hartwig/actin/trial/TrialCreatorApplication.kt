package com.hartwig.actin.trial

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.trial.ctc.CTCConfigInterpreter
import com.hartwig.actin.trial.ctc.config.CTCDatabaseReader
import com.hartwig.actin.trial.interpretation.SimpleConfigInterpreter
import com.hartwig.actin.trial.interpretation.TrialIngestion
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.actin.util.json.GsonSerializer
import com.hartwig.serve.datamodel.serialization.KnownGeneFile
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class TrialCreatorApplication(private val config: TrialCreatorConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Loading known genes from {}", config.knownGenesTsv)
        val knownGenes = KnownGeneFile.read(config.knownGenesTsv)
        LOGGER.info(" Loaded {} known genes", knownGenes.size)
        val geneFilter = GeneFilterFactory.createFromKnownGenes(knownGenes)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val configInterpreter = if (config.ctcConfigDirectory == null) {
            SimpleConfigInterpreter()
        } else {
            CTCConfigInterpreter(CTCDatabaseReader.read(config.ctcConfigDirectory))
        }
        val trialIngestion = TrialIngestion.create(config.trialConfigDirectory, configInterpreter, doidModel, geneFilter, treatmentDatabase)

        LOGGER.info("Creating trial database")
        val result = trialIngestion.ingestTrials()

        val outputDirectory = config.outputDirectory
        LOGGER.info("Writing {} trials to {}", result.trials.size, outputDirectory)
        TrialJson.write(result.trials, outputDirectory)
        LOGGER.info("Done!")

        val resultsJson = Paths.get(outputDirectory).resolve("treatment_ingestion_result.json")
        LOGGER.info("Writing {} trial ingestion results to {}", result.trials.size, resultsJson)
        Files.write(
            resultsJson,
            GsonSerializer.create().toJson(result).toByteArray()
        )
        printAllValidationErrors(result)
    }

    private fun printAllValidationErrors(result: TrialIngestionResult) {
        if (result.ctcDatabaseValidation.hasErrors()) {
            LOGGER.warn("There were validation errors in the CTC database configuration")
            printValidationErrors(result.ctcDatabaseValidation.ctcDatabaseValidationErrors)
            printValidationErrors(result.ctcDatabaseValidation.trialDefinitionValidationErrors)
        }

        if (result.trialValidationResult.hasErrors()) {
            LOGGER.warn("There were validation errors in the trial definition configuration")
            printValidationErrors(result.trialValidationResult.cohortDefinitionValidationErrors)
            printValidationErrors(result.trialValidationResult.trialDefinitionValidationErrors)
            printValidationErrors(result.trialValidationResult.inclusionReferenceValidationErrors)
            printValidationErrors(result.trialValidationResult.inclusionCriteriaValidationErrors)
            printValidationErrors(result.trialValidationResult.unusedRulesToKeepErrors)
        }
    }

    private fun printValidationErrors(errors: Collection<ValidationError<*>>) {
        errors.forEach { LOGGER.warn(it.warningMessage()) }
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(TrialCreatorApplication::class.java)
        const val APPLICATION = "ACTIN Trial Creator"
        private val VERSION: String = TrialCreatorApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options = TrialCreatorConfig.createOptions()
    val config: TrialCreatorConfig
    try {
        config = TrialCreatorConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TrialCreatorApplication.LOGGER.error(exception)
        HelpFormatter().printHelp(TrialCreatorApplication.APPLICATION, options)
        exitProcess(1)
    }
    TrialCreatorApplication(config).run()
}
