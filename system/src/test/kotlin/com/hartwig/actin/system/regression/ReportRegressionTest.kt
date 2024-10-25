package com.hartwig.actin.system.regression

import com.hartwig.actin.system.example.ExampleFunctions
import com.hartwig.actin.system.example.LUNG_01_EXAMPLE
import com.hartwig.actin.system.example.LocalExampleReportApplication
import java.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class ReportRegressionTest(private val exampleName: String) {

    companion object {
        @Parameters
        @JvmStatic
        fun examples() = listOf(LUNG_01_EXAMPLE)
    }

    @Test
    fun `Regress report textually and visually`() {
        System.setProperty("java.locale.providers", "COMPAT")
        val outputDirectory = System.getProperty("user.dir") + "/target/test-classes"
        LocalExampleReportApplication(LocalDate.of(2024, 10, 23)).run(
            ExampleFunctions.resolveExamplePatientRecordJson(exampleName),
            ExampleFunctions.resolveExampleTreatmentMatchJson(exampleName),
            outputDirectory
        )
        assertThatPdf("$outputDirectory/EXAMPLE-$exampleName.actin.pdf").isEqualToTextually("src/test/resources/example_reports/EXAMPLE-$exampleName.actin.pdf")
        assertThatPdf("$outputDirectory/EXAMPLE-$exampleName.actin.pdf").isEqualToVisually("src/test/resources/example_reports/EXAMPLE-$exampleName.actin.pdf")
    }
}