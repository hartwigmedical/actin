package com.hartwig.actin.report

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import com.itextpdf.licensing.base.LicenseKey
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import io.github.oshai.kotlinlogging.KotlinLogging

class ReporterApplication(private val config: ReporterConfig, private val doidModel: DoidModel) {

    fun run() {
        logger.info { "${"Running {} v{}"} $APPLICATION $VERSION" }

        logger.info { "${"Loading patient record from {}"} ${config.patientJson}" }
        val patient = PatientRecordJson.read(config.patientJson)

        logger.info { "${"Loading treatment match results from {}"} ${config.treatmentMatchJson}" }
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson)

        config.itextLicenseKey?.let { key ->
            logger.info { "${"Loading iText license from {}"} $key" }
            LicenseKey.loadLicenseFile(Files.newInputStream(Path.of(key)))
        }

        val configuration = if (config.enableExtendedMode) {
            logger.info { "Extended mode enabled. Using report configuration that includes all possible content" }
            ReportConfiguration.extended()
        } else {
            ReportConfiguration.create(config.overrideYaml)
        }

        val report = ReportFactory.create(config.reportDate ?: LocalDate.now(), patient, treatmentMatch)
        val writer = ReportWriterFactory.createProductionReportWriter(config.outputDirectory)
        writer.write(report, configuration, doidModel, config.enableExtendedMode)
        logger.info { "Done!" }
    }

    companion object {
        const val APPLICATION = "ACTIN Reporter"

        val logger = KotlinLogging.logger {}
        val VERSION = ReporterApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = ReporterConfig.createOptions()
    val config: ReporterConfig
    try {
        config = ReporterConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        ReporterApplication.logger.warn(exception) { exception.message ?: "" }
        HelpFormatter().printHelp(ReporterApplication.APPLICATION, options)
        exitProcess(1)
    }
    ReporterApplication(config, DoidModelFactory.createFromDoidEntry(DoidJson.readDoidOwlEntry(config.doidJson))).run()
}
