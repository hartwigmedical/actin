package com.hartwig.actin.treatment

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.treatment.ctc.CTCModel
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseReader
import com.hartwig.actin.treatment.serialization.TrialJson
import com.hartwig.actin.treatment.trial.EligibilityRuleUsageEvaluator
import com.hartwig.actin.treatment.trial.TrialFactory
import com.hartwig.serve.datamodel.serialization.KnownGeneFile
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess


class TreatmentCreatorApplication(private val config: TreatmentCreatorConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Loading known genes from {}", config.knownGenesTsv)
        val knownGenes = KnownGeneFile.read(config.knownGenesTsv)
        LOGGER.info(" Loaded {} known genes", knownGenes.size)
        val geneFilter = GeneFilterFactory.createFromKnownGenes(knownGenes)

        val ctcModel = CTCModel(CTCDatabaseReader.read(config.ctcConfigDirectory))
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

        val trialFactory = TrialFactory.create(config.trialConfigDirectory, ctcModel, doidModel, geneFilter, treatmentDatabase)
       
        LOGGER.info("Creating trial database")
        val trials = trialFactory.createTrials()

        LOGGER.info("Evaluating usage of eligibility rules")
        EligibilityRuleUsageEvaluator.evaluate(trials)

        val outputDirectory = config.outputDirectory
        LOGGER.info("Writing {} trials to {}", trials.size, outputDirectory)
        TrialJson.write(trials, outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(TreatmentCreatorApplication::class.java)
        const val APPLICATION = "ACTIN Treatment Creator"
        private val VERSION: String = TreatmentCreatorApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TreatmentCreatorConfig.createOptions()
    val config: TreatmentCreatorConfig
    try {
        config = TreatmentCreatorConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TreatmentCreatorApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(TreatmentCreatorApplication.APPLICATION, options)
        exitProcess(1)
    }
    TreatmentCreatorApplication(config).run()
}
