package com.hartwig.actin.report

import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.util.Paths
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

class TabularTreatmentMatchWriterApplication(private val config: TabularTreatmentMatchWriterConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson)

        LOGGER.info("Writing tabular evaluation results to {}", config.outputDirectory)
        val outputPath = Paths.forceTrailingFileSeparator(config.outputDirectory)
        val evaluationSummaryTsv = outputPath + treatmentMatch.patientId + ".evaluation.summary.tsv"
        TabularTreatmentMatchWriter.writeEvaluationSummaryToTsv(treatmentMatch, evaluationSummaryTsv)
        LOGGER.info(" Written summary data to {}", evaluationSummaryTsv)

        val evaluationDetailsTsv = outputPath + treatmentMatch.patientId + ".evaluation.details.tsv"
        TabularTreatmentMatchWriter.writeEvaluationDetailsToTsv(treatmentMatch, evaluationDetailsTsv)
        LOGGER.info(" Written detailed data to {}", evaluationDetailsTsv)
        LOGGER.info("Done!")
    }

    companion object {
        const val APPLICATION = "ACTIN Tabular Treatment Match Writer"

        val LOGGER: Logger = LogManager.getLogger(TabularTreatmentMatchWriterApplication::class.java)
        private val VERSION = TabularTreatmentMatchWriterApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TabularTreatmentMatchWriterConfig.createOptions()
    val config: TabularTreatmentMatchWriterConfig
    try {
        config = TabularTreatmentMatchWriterConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TabularTreatmentMatchWriterApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(TabularTreatmentMatchWriterApplication.APPLICATION, options)
        exitProcess(1)
    }

    TabularTreatmentMatchWriterApplication(config).run()
}