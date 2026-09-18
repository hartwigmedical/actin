package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.configuration.ReportType
import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import com.hartwig.actin.datamodel.clinical.TestOtherConditionFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.CellTestUtil.extractTextFromCell
import com.hartwig.actin.util.ApplicationConfig
import com.itextpdf.layout.element.Table
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val KEY_WIDTH = 100f
private const val VALUE_WIDTH = 200f

private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", ApplicationConfig.LOCALE)

class PatientClinicalHistoryGeneratorTest {

    private val report = TestReportFactory.createMinimalTestReport()
    private val labels = ReportLabels.load(ReportType.TRIAL_MATCHING_RESEARCH_USE_ONLY)

    @Test
    fun `Should return title search summary for research use only labels`() {
        val clinicalSummaryGenerator = ClinicalSummaryGenerator(report, true, KEY_WIDTH, VALUE_WIDTH, labels)
        assertThat(clinicalSummaryGenerator.title()).isEqualTo("Search summary")
    }

    @Test
    fun `Should return title search summary`() {
        val nonMedicalLabels = ReportLabels.load(ReportType.TRIAL_MATCHING_NON_MEDICAL)
        val clinicalSummaryGenerator = ClinicalSummaryGenerator(report, true, KEY_WIDTH, VALUE_WIDTH, nonMedicalLabels)
        assertThat(clinicalSummaryGenerator.title()).isEqualTo("Search summary")
    }

    @Test
    fun `Should return content table with surgeries including surgery name`() {
        val endDate = LocalDate.of(2024, 9, 19)
        val endDateMinus6 = endDate.minusDays(6)
        val endDateMinus4 = endDate.minusDays(4)

        val reportWithSurgeries = report.copy(
            patientRecord = report.patientRecord.copy(
                surgeries = listOf(
                    Surgery(
                        name = "Surgery 2",
                        endDateMinus6,
                        status = SurgeryStatus.FINISHED,
                        treatmentType = OtherTreatmentType.DEBULKING_SURGERY
                    ),
                    Surgery(
                        name = "Surgery 1",
                        endDate,
                        status = SurgeryStatus.FINISHED,
                        treatmentType = OtherTreatmentType.CYTOREDUCTIVE_SURGERY
                    ),
                    Surgery(name = null, endDateMinus4, status = SurgeryStatus.FINISHED, treatmentType = OtherTreatmentType.OTHER_SURGERY)
                )
            )
        )


        val clinicalSummaryGenerator = ClinicalSummaryGenerator(reportWithSurgeries, true, KEY_WIDTH, VALUE_WIDTH, labels)
        val cells = clinicalSummaryGenerator.contentsAsList()
        val surgeriesCell = cells.dropWhile { extractTextFromCell(it) != "Recent surgeries" }.drop(1).first()
        assertThat(surgeriesCell).isNotNull
        assertThat(extractTextFromCell(surgeriesCell)).isEqualTo(
            "%s Surgery 1, %s, %s Surgery 2",
            DATE_FORMAT.format(endDate),
            DATE_FORMAT.format(endDateMinus4),
            DATE_FORMAT.format(endDateMinus6)
        )
    }


    @Test
    fun `Should return content as list with sorted other prior conditions`() {
        val reportWithOtherConditions = report.copy(
            patientRecord = report.patientRecord.copy(
                comorbidities = listOf(
                    TestOtherConditionFactory.create("c1", null, null),
                    TestOtherConditionFactory.create("c2", 2024, null),
                    TestOtherConditionFactory.create("c3", 2024, 8),
                    TestOtherConditionFactory.create("c4", 2024, 5),
                    TestOtherConditionFactory.create("c5", 2023, 9),
                    TestOtherConditionFactory.create("c6", null, 2)
                )
            )
        )

        val otherHistoryTable = generateHistoryAndReturnTableWithText(reportWithOtherConditions, "Non-oncological history")

        assertThat(otherHistoryTable.numberOfRows).isEqualTo(6)
        assertThat(extractTextFromCell(otherHistoryTable.getCell(0, 0))).isEqualTo("2024-08")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(1, 0))).isEqualTo("2024-05")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(2, 0))).isEqualTo("2024")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(3, 0))).isEqualTo("2023-09")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(4, 0))).isEqualTo("Date unknown")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(5, 0))).isEqualTo("Date unknown")
    }

    @Test
    fun `Should return content as list with sorted other systemic treatment history`() {
        val reportWithOncologicalHistoryAndMedications = report.copy(
            patientRecord = report.patientRecord.copy(
                oncologicalHistory = listOf(
                    TreatmentTestFactory.treatmentHistoryEntry(
                        setOf(
                            TreatmentTestFactory.drugTreatment(
                                "Chemotherapy",
                                TreatmentCategory.CHEMOTHERAPY
                            )
                        ), startYear = 2022
                    )
                ), medications = listOf(
                    TestMedicationFactory.createMinimal().copy(
                        drug = Drug(
                            name = "Pembrolizumab",
                            category = TreatmentCategory.IMMUNOTHERAPY,
                            drugTypes = setOf(DrugType.PD_1_PD_L1_ANTIBODY)
                        ), name = "Pembrolizumab", atc = AtcClassification(
                            anatomicalMainGroup = AtcLevel(name = "", code = "L"),
                            chemicalSubGroup = AtcLevel(name = "", code = "L01"),
                            chemicalSubstance = AtcLevel(name = "", code = "L01F"),
                            pharmacologicalSubGroup = AtcLevel(name = "", code = "L01FF"),
                            therapeuticSubGroup = AtcLevel(name = "", code = "L01FF02")
                        ), startDate = LocalDate.of(2023, 12, 12)
                    )
                )
            )
        )

        val otherHistoryTable =
            generateHistoryAndReturnTableWithText(reportWithOncologicalHistoryAndMedications, "Systemic treatment history")

        assertThat(otherHistoryTable.numberOfRows).isEqualTo(2)
        assertThat(extractTextFromCell(otherHistoryTable.getCell(0, 0))).isEqualTo("2022")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(0, 1))).isEqualTo("Chemotherapy")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(1, 0))).isEqualTo("2023-12")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(1, 1))).isEqualTo("Pembrolizumab")
    }

    @Test
    fun `Should omit PD stop reason detail from treatment string`() {
        assertTreatmentSummaryWithStopReasonDetailOmitted("PD")
    }

    @Test
    fun `Should omit empty stop reason detail from treatment string`() {
        assertTreatmentSummaryWithStopReasonDetailOmitted("")
    }

    @Test
    fun `Should omit blank reason detail from treatment string`() {
        assertTreatmentSummaryWithStopReasonDetailOmitted(" ")
    }

    private fun assertTreatmentSummaryWithStopReasonDetailOmitted(stopReasonDetail: String) {
        val reportWithStopReason = report.copy(
            patientRecord = report.patientRecord.copy(
                oncologicalHistory = listOf(
                    TreatmentTestFactory.treatmentHistoryEntry(
                        treatments = setOf(TreatmentTestFactory.drugTreatment("Chemotherapy", TreatmentCategory.CHEMOTHERAPY)),
                        startYear = 2023,
                        numCycles = 4,
                        stopReasonDetail = stopReasonDetail
                    )
                )
            )
        )

        val systemicHistoryTable =
            generateHistoryAndReturnTableWithText(reportWithStopReason, "Systemic treatment history")

        assertThat(systemicHistoryTable.numberOfRows).isEqualTo(1)
        assertThat(extractTextFromCell(systemicHistoryTable.getCell(0, 1))).isEqualTo("Chemotherapy (4 cycles)")
    }

    private fun generateHistoryAndReturnTableWithText(report: Report, cellToFind: String): Table {
        val clinicalSummaryGenerator = ClinicalSummaryGenerator(report, true, KEY_WIDTH, VALUE_WIDTH, labels)
        val cells = clinicalSummaryGenerator.contentsAsList()
        val otherHistoryCell = cells.dropWhile { extractTextFromCell(it) != cellToFind }.drop(1).first()
        return otherHistoryCell.children.first() as? Table ?: throw IllegalStateException("Expected Table as first child")
    }
}
