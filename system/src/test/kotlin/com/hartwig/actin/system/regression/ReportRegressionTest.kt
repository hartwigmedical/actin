package com.hartwig.actin.system.regression

import ch.qos.logback.classic.Level
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.ReportType
import com.hartwig.actin.system.example.CRC_01_EXAMPLE
import com.hartwig.actin.system.example.ExampleFunctions
import com.hartwig.actin.system.example.LUNG_01_EXAMPLE
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReportRegressionTest {

    private val logLevelRecorder = LogLevelRecorder()

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
    fun `Regress ruo trial matching report textually and visually`() {
        regressReport(exampleName = LUNG_01_EXAMPLE, outputReportSufix = "ruo") {
            ExampleFunctions.createTrialMatchingReportConfiguration().copy(reportType = ReportType.TRIAL_MATCHING_RESEARCH_USE_ONLY)
        }
    }

    @Test
    fun `Regress personalization report textually and visually`() {
        regressReport(exampleName = CRC_01_EXAMPLE) { ExampleFunctions.createPersonalizationReportConfiguration() }
    }

    private fun regressReport(exampleName: String, outputReportSufix: String? = null, reportConfigProvider: () -> ReportConfiguration) {
        val outputDirectory = System.getProperty("user.dir") + "/target/test-classes"

        ExampleFunctions.run(
            LocalDate.of(2025, 9, 17),
            ExampleFunctions.resolveExamplePatientRecordJson(exampleName),
            ExampleFunctions.resolveExampleTreatmentMatchJson(exampleName),
            outputDirectory,
            reportConfigProvider()
        )

        assertThat(logLevelRecorder.levelRecorded(Level.WARN) || logLevelRecorder.levelRecorded(Level.ERROR))
            .withFailMessage("There are errors or warnings in the logs")
            .isFalse()

        val suffixPart = if (!outputReportSufix.isNullOrEmpty()) ".$outputReportSufix" else ""
        val outputReportPdf = "$outputDirectory/EXAMPLE-$exampleName.actin.pdf"
        val originalReportPdf = ExampleFunctions.resolveExampleReportPdf("$exampleName$suffixPart")
        assertThatPdf(outputReportPdf).isEqualToTextually(originalReportPdf)
        assertThatPdf(outputReportPdf).isEqualToVisually(originalReportPdf)
    }
}
