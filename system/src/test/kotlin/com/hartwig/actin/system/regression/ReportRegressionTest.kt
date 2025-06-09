package com.hartwig.actin.system.regression

import com.hartwig.actin.system.example.ExampleFunctions
import com.hartwig.actin.system.example.LUNG_01_EXAMPLE
import com.hartwig.actin.system.example.LocalExampleReportApplication
import org.apache.logging.log4j.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.time.LocalDate
import java.util.Locale
import org.junit.Ignore

@Ignore
@RunWith(Parameterized::class)
class ReportRegressionTest(private val exampleName: String) {

    private val logLevelRecorder = LogLevelRecorder()

    companion object {
        private val originalLocale = Locale.getDefault()

        @Parameters
        @JvmStatic
        fun examples() = listOf(LUNG_01_EXAMPLE)

        @BeforeClass
        @JvmStatic
        fun setReportLocale() = Locale.setDefault(Locale.US)

        @AfterClass
        @JvmStatic
        fun revertReportLocale() = Locale.setDefault(originalLocale)
    }

    @Before
    fun setUp() {
        logLevelRecorder.start()
    }

    @After
    fun tearDown() {
        logLevelRecorder.stop()
    }

    @Test
    fun `Regress report textually and visually`() {
        val outputDirectory = System.getProperty("user.dir") + "/target/test-classes"
        val localExampleReportApplication = LocalExampleReportApplication()

        localExampleReportApplication.run(
            ExampleFunctions.resolveExamplePatientRecordJson(exampleName),
            ExampleFunctions.resolveExampleTreatmentMatchJson(exampleName),
            outputDirectory,
            ExampleFunctions.createExhaustiveEnvironmentConfiguration(LocalDate.of(2025, 4, 17))
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