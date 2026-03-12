package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.CUP_STRING
import com.hartwig.actin.report.pdf.getCellContents
import com.hartwig.actin.report.pdf.tables.soc.EligibleStandardOfCareGenerator
import com.itextpdf.layout.element.Table
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EligibleApprovedTreatmentGeneratorTest {

    @Test
    fun `Should return no treatment options if there are no treatments`() {
        val contents = eligibleTreatmentsTable()
        assertThat(getCellContents(contents, 0, 0)).isEqualTo("There are no standard of care treatment options for this patient")
    }

    private fun eligibleTreatmentsTable(
        clinicalRecord: ClinicalRecord = TestClinicalFactory.createMinimalTestClinicalRecord(),
        molecularTests: List<MolecularTest> = emptyList(),
        treatmentMatch: TreatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch()
    ): Table {
        val report = TestReportFactory.createMinimalTestReport().copy(
            patientRecord = PatientRecordFactory.fromInputs(clinicalRecord, molecularTests),
            treatmentMatch = treatmentMatch
        )

        return EligibleStandardOfCareGenerator(report).contents()
    }
}

