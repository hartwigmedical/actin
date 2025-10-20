package com.hartwig.actin.report

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.LocalDate
import kotlin.system.exitProcess

class ReporterApplication(private val config: ReporterConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading patient record from {}", config.patientJson)
        val patient = PatientRecordJson.read(config.patientJson)

        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson)

        val configuration = if (config.enableExtendedMode) {
            LOGGER.info("Extended mode enabled. Using report configuration that includes all possible content")
            ReportConfiguration.extended()
        } else {
            ReportConfiguration.create(config.overrideYaml)
        }

        val report = ReportFactory.create(config.reportDate ?: LocalDate.now(), patient, treatmentMatch)
        val writer = ReportWriterFactory.createProductionReportWriter(config.outputDirectory)
        writer.write(report, configuration, config.enableExtendedMode)
        LOGGER.info("Done!")
    }

    companion object {
        const val APPLICATION = "ACTIN Reporter"

        val LOGGER: Logger = LogManager.getLogger(ReporterApplication::class.java)
        val VERSION = ReporterApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = ReporterConfig.createOptions()
    val config: ReporterConfig
    try {
        config = ReporterConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        ReporterApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(ReporterApplication.APPLICATION, options)
        exitProcess(1)
    }

    ReporterApplication(config).run()
}
