package com.hartwig.actin.report

import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException

class ReporterApplication private constructor(private val config: ReporterConfig) {
    @Throws(IOException::class)
    fun run() {
        LOGGER.info("Loading clinical record from {}", config.clinicalJson())
        val clinical = ClinicalRecordJson.read(config.clinicalJson())
        LOGGER.info("Loading molecular record from {}", config.molecularJson())
        val molecular = MolecularRecordJson.read(config.molecularJson())
        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson())
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson())
        val report = ReportFactory.fromInputs(clinical, molecular, treatmentMatch)
        val writer = ReportWriterFactory.createProductionReportWriter(config.outputDirectory())
        writer.write(report, config.enableExtendedMode())
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ReporterApplication::class.java)
        private const val APPLICATION = "ACTIN Reporter"
        val VERSION = ReporterApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            LOGGER.info("Running {} v{}", APPLICATION, VERSION)
            val options: Options = ReporterConfig.Companion.createOptions()
            var config: ReporterConfig? = null
            try {
                config = ReporterConfig.Companion.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            ReporterApplication(config!!).run()
        }
    }
}