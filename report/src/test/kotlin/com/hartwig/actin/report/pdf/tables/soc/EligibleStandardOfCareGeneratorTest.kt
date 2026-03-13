package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.getCellContents
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EligibleStandardOfCareGeneratorTest {

    @Test
    fun `Should return no treatment options if there are no treatments`() {
        val report = TestReportFactory.createMinimalTestReport().copy(
            patientRecord = PatientRecordFactory.fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                emptyList()
            ),
            treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch()
        )

        val contents = EligibleStandardOfCareGenerator(report).contents()

        assertThat(getCellContents(contents, 0, 0)).isEqualTo("There are no standard of care treatment options for this patient")
    }
}