package com.hartwig.actin.report

import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class ReporterApplication(private val config: ReporterConfig) {

    fun run() {
        LOGGER.info("Loading clinical record from {}", config.clinicalJson)
        val clinical = ClinicalRecordJson.read(config.clinicalJson)

        val molecular: MolecularRecord? = config.molecularJson?.let {
            LOGGER.info("Loading molecular record from {}", config.molecularJson)
            MolecularRecordJson.read(config.molecularJson)
        }

        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson)

        val report = ReportFactory.fromInputs(clinical, molecular, treatmentMatch)
        val writer = ReportWriterFactory.createProductionReportWriter(config.outputDirectory)
        writer.write(report, config.enableExtendedMode)
        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(ReporterApplication::class.java)
        const val APPLICATION = "ACTIN Reporter"
        val VERSION: String? = ReporterApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>) {
    ReporterApplication.LOGGER.info("Running {} v{}", ReporterApplication.APPLICATION, ReporterApplication.VERSION)
    val options: Options = ReporterConfig.createOptions()
    try {
        val config = ReporterConfig.createConfig(DefaultParser().parse(options, args))
        ReporterApplication(config).run()
    } catch (exception: ParseException) {
        ReporterApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(ReporterApplication.APPLICATION, options)
        exitProcess(1)
    }
}
