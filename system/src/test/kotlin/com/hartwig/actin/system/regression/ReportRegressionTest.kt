package com.hartwig.actin.system.regression

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.system.example.CRC_01_EXAMPLE
import com.hartwig.actin.system.example.ExampleFunctions
import com.hartwig.actin.system.example.LUNG_01_EXAMPLE
import java.time.LocalDate
import java.util.Locale
import org.apache.logging.log4j.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReportRegressionTest {

    private val logLevelRecorder = LogLevelRecorder()

    companion object {
        private val originalLocale = Locale.getDefault()

        @BeforeAll
        @JvmStatic
        fun setReportLocale() = Locale.setDefault(Locale.US)

        @AfterAll
        @JvmStatic
        fun revertReportLocale() = Locale.setDefault(originalLocale)
    }

    @BeforeEach
    fun setUp() {
        logLevelRecorder.start()
    }

    @AfterEach
    fun tearDown() {
        logLevelRecorder.stop()
    }

    @Test
    fun `Regress trial matching report textually and visually`() {
        regressReport(exampleName = LUNG_01_EXAMPLE) { ExampleFunctions.createTrialMatchingReportConfiguration() }
    }

    @Test
    fun `Regress personalization report textually and visually`() {
        regressReport(exampleName = CRC_01_EXAMPLE) { ExampleFunctions.createPersonalizationReportConfiguration() }
    }

    private fun regressReport(exampleName: String, reportConfigProvider: () -> ReportConfiguration) {
        val outputDirectory = System.getProperty("user.dir") + "/target/test-classes"

        ExampleFunctions.run(
            LocalDate.of(2025, 9, 17),
            ExampleFunctions.resolveExamplePatientRecordJson(exampleName),
            ExampleFunctions.resolveExampleTreatmentMatchJson(exampleName),
            outputDirectory,
            reportConfigProvider()
        )

        assertThat(logLevelRecorder.levelRecorded(Level.WARN) || logLevelRecorder.levelRecorded(Level.ERROR))
            .withFailMessage("There are errors or warnings in the logs").isFalse()

        val outputReportPdf = "$outputDirectory/EXAMPLE-$exampleName.actin.pdf"
        val originalReportPdf = ExampleFunctions.resolveExampleReportPdf(exampleName)
        assertThatPdf(outputReportPdf).isEqualToTextually(originalReportPdf)
        assertThatPdf(outputReportPdf).isEqualToVisually(originalReportPdf)

        val outputExtendedReportPdf = "$outputDirectory/EXAMPLE-$exampleName.actin.extended.pdf"
        val originalExtendedReportPdf = ExampleFunctions.resolveExampleReportExtendedPdf(exampleName)
        assertThatPdf(outputExtendedReportPdf).isEqualToTextually(outputExtendedReportPdf)
        assertThatPdf(outputExtendedReportPdf).isEqualToVisually(originalExtendedReportPdf)
    }
}