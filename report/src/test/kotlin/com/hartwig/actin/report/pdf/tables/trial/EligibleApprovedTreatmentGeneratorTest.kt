package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.CUP_LOCATION
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter.CUP_SUB_LOCATION
import com.hartwig.actin.report.pdf.getCellContents
import com.hartwig.actin.report.pdf.getWrappedTable
import com.itextpdf.layout.element.Table
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val width: Float = 10F

class EligibleApprovedTreatmentGeneratorTest {
    @Test
    fun `Should return approved treatments if available`() {
        val contents = eligibleTreatmentsTable(treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch())
        assertThat(getCellContents(contents, 0, 0)).isEqualTo("Pembrolizumab")
    }

    @Test
    fun `Should return Potential SOC if it is a cancer of unknown primary`() {
        val cupTumor = TumorDetails(primaryTumorLocation = CUP_LOCATION, primaryTumorSubLocation = CUP_SUB_LOCATION)
        val clinicalRecord = TestClinicalFactory.createProperTestClinicalRecord().copy(tumor = cupTumor)

        val molecularHistory = MolecularHistory(
            listOf(
                TestMolecularFactory.createMinimalTestOrangeRecord().copy(
                    characteristics = MolecularCharacteristics(
                        predictedTumorOrigin = PredictedTumorOrigin(
                            listOf(
                                CupPrediction("colorectal", 0.9, 0.0, 0.0, 0.0)
                            )
                        )
                    )
                )
            )
        )

        val contents = eligibleTreatmentsTable(clinicalRecord, molecularHistory)
        assertThat(getCellContents(contents, 0, 0)).isEqualTo("Potential SOC for colorectal")
    }

    @Test
    fun `Should return Not Yet Determined if there are no approved treatments or cancer of unknown primary`() {
        val contents = eligibleTreatmentsTable()
        assertThat(getCellContents(contents, 0, 0)).isEqualTo("Not yet determined")
    }

    private fun eligibleTreatmentsTable(
        clinicalRecord: ClinicalRecord = TestClinicalFactory.createMinimalTestClinicalRecord(),
        molecularHistory: MolecularHistory = MolecularHistory.empty(),
        treatmentMatch: TreatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch()
    ): Table {
        val report = ReportFactory.fromInputs(
            PatientRecordFactory.fromInputs(clinicalRecord, molecularHistory),
            treatmentMatch,
            EnvironmentConfiguration.create(null)
        )
        return getWrappedTable(EligibleApprovedTreatmentGenerator(report, width))
    }
}

