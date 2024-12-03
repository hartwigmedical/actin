package com.hartwig.actin.trial

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.configuration.TrialConfiguration
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.trial.interpretation.TrialIngestion
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.actin.trial.status.TrialStatusConfigInterpreter
import com.hartwig.actin.trial.status.TrialStatusDatabaseReader
import com.hartwig.actin.trial.status.ctc.CTCTrialStatusEntryReader
import com.hartwig.actin.trial.status.nki.NKITrialStatusEntryReader
import com.hartwig.serve.datamodel.RefGenome
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Paths
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
        val knownGenes = ServeLoader.load(config.serveDbJson, RefGenome.V37).knownEvents().genes()
        LOGGER.info(" Loaded {} known genes", knownGenes.size)

        val geneFilter = GeneFilterFactory.createFromKnownGenes(knownGenes)

        val configInterpreter = configInterpreter(EnvironmentConfiguration.create(config.overridesYaml).trial)
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

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
        // TODO (KD): Would potentially be nicer to return null trials in case trial ingestion was explicitly skipped due to errors
        if (result.trialConfigDatabaseValidation.hasErrors() && result.trials.isEmpty()) {
            LOGGER.warn("No trials were created due to presence of trial config database validation errors")
        } else {
            LOGGER.info("Writing {} trials to {}", result.trials.size, outputDirectory)
            TrialJson.write(result.trials, outputDirectory)
        }

        val resultsJson = Paths.get(outputDirectory).resolve("treatment_ingestion_result.json")
        LOGGER.info("Writing trial ingestion results to {}", resultsJson)
        Files.write(
            resultsJson,
            result.serialize().toByteArray()
        )

        printAllValidationErrors(result)
        LOGGER.info("Done!")
    }

    private fun configInterpreter(configuration: TrialConfiguration): TrialStatusConfigInterpreter {
        LOGGER.info(" Using trial configuration: $configuration")

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
            throw IllegalArgumentException("At least one of CTC and NKI config directories must be specified")
        }
    }

    private fun printAllValidationErrors(result: TrialIngestionResult) {
        if (result.trialConfigDatabaseValidation.hasErrors()) {
            LOGGER.warn("There were validation errors in the trial definition configuration")
            printValidationErrors(result.trialConfigDatabaseValidation.cohortDefinitionValidationErrors)
            printValidationErrors(result.trialConfigDatabaseValidation.trialDefinitionValidationErrors)
            printValidationErrors(result.trialConfigDatabaseValidation.inclusionCriteriaReferenceValidationErrors)
            printValidationErrors(result.trialConfigDatabaseValidation.inclusionCriteriaValidationErrors)
            printValidationErrors(result.trialConfigDatabaseValidation.unusedRulesToKeepValidationErrors)
        }

        if (result.trialStatusDatabaseValidation.hasErrors()) {
            LOGGER.warn("There were validation errors in the trial status database configuration")
            printValidationErrors(result.trialStatusDatabaseValidation.trialStatusDatabaseValidationErrors)
            printValidationErrors(result.trialStatusDatabaseValidation.trialStatusConfigValidationErrors)
        }
    }

    private fun printValidationErrors(errors: Collection<ValidationError<*>>) {
        errors.forEach { LOGGER.warn(it.warningMessage()) }
    }

    companion object {
        const val APPLICATION = "ACTIN Trial Creator"

        val LOGGER: Logger = LogManager.getLogger(TrialCreatorApplication::class.java)
        private val VERSION = TrialCreatorApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
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
