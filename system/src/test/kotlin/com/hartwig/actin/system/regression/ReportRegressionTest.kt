package com.hartwig.actin.system.regression

import com.hartwig.actin.system.example.ExampleFunctions
import com.hartwig.actin.system.example.LocalExampleReportApplication
import org.junit.Test

class ReportRegressionTest {

    @Test
    fun `Regress EXAMPLE-LUNG-01 text`() {
        val examplePatientRecordJson = ExampleFunctions.resolveExamplePatientRecordJson()
        val exampleTreatmentMatchJson = ExampleFunctions.resolveExampleTreatmentMatchJson()
        val outputDirectory = System.getProperty("user.dir") + "/target/test-classes"
        LocalExampleReportApplication().run(examplePatientRecordJson, exampleTreatmentMatchJson, outputDirectory)
        assertThatPdf("$outputDirectory/EXAMPLE-LUNG-01.actin.pdf").isEqualTo("src/test/resources/example_reports/EXAMPLE-LUNG-01.actin.pdf")
    }

    @Test
    fun `Regress EXAMPLE-LUNG-01 visual`() {
        val examplePatientRecordJson = ExampleFunctions.resolveExamplePatientRecordJson()
        val exampleTreatmentMatchJson = ExampleFunctions.resolveExampleTreatmentMatchJson()
        val outputDirectory = System.getProperty("user.dir") + "/target/test-classes"
        LocalExampleReportApplication().run(examplePatientRecordJson, exampleTreatmentMatchJson, outputDirectory)
        assertThatPdf("$outputDirectory/EXAMPLE-LUNG-01.actin.pdf").isEqualToVisually("src/test/resources/example_reports/EXAMPLE-LUNG-01.actin.pdf")
    }
}