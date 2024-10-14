package com.hartwig.actin.trial

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.configuration.TrialConfiguration
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.trial.interpretation.ConfigInterpreter
import com.hartwig.actin.trial.interpretation.SimpleConfigInterpreter
import com.hartwig.actin.trial.interpretation.TrialIngestion
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.actin.trial.status.TrialStatusConfigInterpreter
import com.hartwig.actin.trial.status.TrialStatusDatabaseReader
import com.hartwig.actin.trial.status.ctc.CTCTrialStatusEntryReader
import com.hartwig.actin.trial.status.nki.NKITrialStatusEntryReader
import com.hartwig.serve.datamodel.serialization.ServeJson
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

const val CTC_TRIAL_PREFIX = "MEC"

class TrialCreatorApplication(private val config: TrialCreatorConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Loading known genes from serve db {}", config.serveDbJson)
        val knownGenes = ServeJson.read(config.serveDbJson).knownEvents().genes()
        LOGGER.info(" Loaded {} known genes", knownGenes.size)
        val geneFilter = GeneFilterFactory.createFromKnownGenes(knownGenes)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val configInterpreter = configInterpreter(EnvironmentConfiguration.create(config.overridesYaml).trial)
        LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        val trialIngestion = TrialIngestion.create(
            config.trialConfigDirectory,
            configInterpreter,
            doidModel,
            geneFilter,
            treatmentDatabase,
            MedicationCategories.create(atcTree)
        )

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
            result.serialize().toByteArray()
        )
        printAllValidationErrors(result)
    }

    private fun configInterpreter(configuration: TrialConfiguration): ConfigInterpreter {
        if (config.ctcConfigDirectory != null && config.nkiConfigDirectory != null) {
            throw IllegalArgumentException("Only one of CTC and NKI config directories can be specified")
        }

        return if (config.ctcConfigDirectory != null) {
            TrialStatusConfigInterpreter(
                TrialStatusDatabaseReader(CTCTrialStatusEntryReader()).read(config.ctcConfigDirectory),
                CTC_TRIAL_PREFIX,
                ignoreNewTrials = configuration.ignoreAllNewTrialsInTrialStatusDatabase
            )
        } else if (config.nkiConfigDirectory != null) {
            TrialStatusConfigInterpreter(
                TrialStatusDatabaseReader(NKITrialStatusEntryReader()).read(config.nkiConfigDirectory),
                ignoreNewTrials = configuration.ignoreAllNewTrialsInTrialStatusDatabase
            )
        } else {
            SimpleConfigInterpreter()
        }
    }

    private fun printAllValidationErrors(result: TrialIngestionResult) {
        if (result.trialStatusDatabaseValidation.hasErrors()) {
            LOGGER.warn("There were validation errors in the trial status database configuration")
            printValidationErrors(result.trialStatusDatabaseValidation.trialStatusDatabaseValidationErrors)
            printValidationErrors(result.trialStatusDatabaseValidation.trialDefinitionValidationErrors)
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
