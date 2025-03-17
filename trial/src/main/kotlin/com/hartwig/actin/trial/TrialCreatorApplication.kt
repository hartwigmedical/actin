package com.hartwig.actin.trial

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.serialization.CsvReader
import com.hartwig.actin.icd.serialization.IcdDeserializer
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.actin.util.Either
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

const val ERROR_JSON_FILE = "trial_ingestion_errors.json"

class TrialCreatorApplication(private val config: TrialCreatorConfig) {

    fun run() {
        LOGGER.info("Running $APPLICATION v$VERSION")

        LOGGER.info("Loading DOID tree from ${config.doidJson}")
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded ${doidEntry.nodes.size} nodes")
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ICD-11 tree from file ${config.icdTsv}")
        val icdNodes = IcdDeserializer.deserialize(CsvReader.readFromFile(config.icdTsv))
        LOGGER.info(" Loaded ${icdNodes.size} nodes")
        val icdModel = IcdModel.create(icdNodes)

        LOGGER.info("Loading SERVE known genes from ${config.serveDbJson}")
        val knownGenes = ServeLoader.loadServe37Record(config.serveDbJson).knownEvents().genes()
        LOGGER.info(" Loaded ${knownGenes.size} known genes")

        val geneFilter = GeneFilterFactory.createFromKnownGenes(knownGenes)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

        LOGGER.info("Creating ATC tree from file ${config.atcTsv}")
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        val trialIngestion =
            TrialIngestion(
                EligibilityFactory(
                    FunctionInputResolver(
                        doidModel,
                        icdModel,
                        MolecularInputChecker(geneFilter),
                        treatmentDatabase,
                        MedicationCategories.create(atcTree)
                    )
                )
            )

        LOGGER.info("Creating trial database")
        val objectMapper = ObjectMapper().apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            registerModule(KotlinModule.Builder().build())
        }
        val result =
            trialIngestion.ingest(objectMapper.readValue(File(config.trialConfigJsonPath), object : TypeReference<List<TrialConfig>>() {}))

        val outputDirectory = config.outputDirectory
        when (result) {
            is Either.Right -> {
                LOGGER.info("Writing ${result.value.size} trials to [$outputDirectory]")
                TrialJson.write(result.value, outputDirectory)
                LOGGER.info("Writing list of proteins referenced in inclusion criteria to [$outputDirectory]")
                Files.write(Path.of(outputDirectory, "ihc_proteins.list"), EligibilityRuleUsageEvaluator.extractIhcProteins(result.value))
            }

            is Either.Left -> {
                Files.write(Path.of(outputDirectory).resolve(ERROR_JSON_FILE), objectMapper.writeValueAsBytes(result.value))
                exitProcess(1)
            }
        }

        LOGGER.info("$APPLICATION done!")
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
