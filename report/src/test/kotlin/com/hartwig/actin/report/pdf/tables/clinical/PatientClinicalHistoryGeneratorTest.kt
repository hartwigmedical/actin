package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import com.hartwig.actin.datamodel.clinical.TestPriorOtherConditionFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil.extractTextFromCell
import com.itextpdf.layout.element.Table
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val KEY_WIDTH = 100f
private const val VALUE_WIDTH = 200f

class PatientClinicalHistoryGeneratorTest {

    private val report = TestReportFactory.createMinimalTestReport()

    @Test
    fun `Should return title clinical summary`() {
        val patientClinicalHistoryGenerator = PatientClinicalHistoryGenerator(report, true, KEY_WIDTH, VALUE_WIDTH)
        assertThat(patientClinicalHistoryGenerator.title()).isEqualTo("Clinical summary")
    }

    @Test
    fun `Should return content as list with sorted other prior conditions`() {
        val reportWithOtherConditions = report.copy(
            patientRecord = report.patientRecord.copy(
                priorOtherConditions = listOf(
                    TestPriorOtherConditionFactory.create("c1", null, null),
                    TestPriorOtherConditionFactory.create("c2", 2024, null),
                    TestPriorOtherConditionFactory.create("c3", 2024, 8),
                    TestPriorOtherConditionFactory.create("c4", 2024, 5),
                    TestPriorOtherConditionFactory.create("c5", 2023, 9),
                    TestPriorOtherConditionFactory.create("c6", null, 2)
                )
            )
        )

        val otherHistoryTable = createTable(reportWithOtherConditions, "Relevant non-oncological history")

        assertThat(otherHistoryTable.numberOfRows).isEqualTo(6)
        assertThat(extractTextFromCell(otherHistoryTable.getCell(0, 0))).isEqualTo("8/2024")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(1, 0))).isEqualTo("5/2024")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(2, 0))).isEqualTo("2024")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(3, 0))).isEqualTo("9/2023")
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

        val otherHistoryTable = createTable(reportWithOncologicalHistoryAndMedications, "Relevant systemic treatment history")

        assertThat(otherHistoryTable.numberOfRows).isEqualTo(2)
        assertThat(extractTextFromCell(otherHistoryTable.getCell(0, 0))).isEqualTo("2022")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(0, 1))).isEqualTo("Chemotherapy")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(1, 0))).isEqualTo("12/2023")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(1, 1))).isEqualTo("Pembrolizumab")
    }

    private fun createTable(report: Report, cellToFind: String): Table {
        val patientClinicalHistoryGenerator = PatientClinicalHistoryGenerator(report, true, KEY_WIDTH, VALUE_WIDTH)
        val cells = patientClinicalHistoryGenerator.contentsAsList()
        val otherHistoryCell =
            cells.dropWhile { extractTextFromCell(it) != cellToFind }.drop(1).first()
        return otherHistoryCell.children.first() as? Table ?: throw IllegalStateException("Expected Table as first child")
    }
}