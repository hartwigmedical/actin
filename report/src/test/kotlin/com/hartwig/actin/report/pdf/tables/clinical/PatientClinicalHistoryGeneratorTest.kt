package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

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
                priorOtherConditions = createTestPriorOtherConditions()
            )
        )

        val patientClinicalHistoryGenerator = PatientClinicalHistoryGenerator(reportWithOtherConditions, true, KEY_WIDTH, VALUE_WIDTH)
        val cells = patientClinicalHistoryGenerator.contentsAsList()

        for ((i, cell) in cells.withIndex()) {
            if (extractTextFromCell(cell) == "Relevant non-oncological history") {
                val otherHistoryTable: Table = cells.getOrNull(i + 1)?.children?.firstOrNull() as? Table ?: continue
                assertThat(otherHistoryTable.numberOfRows).isEqualTo(6)
                assertThat(extractTextFromCell(otherHistoryTable.getCell(0, 0))).isEqualTo("8/2024")
                assertThat(extractTextFromCell(otherHistoryTable.getCell(1, 0))).isEqualTo("5/2024")
                assertThat(extractTextFromCell(otherHistoryTable.getCell(2, 0))).isEqualTo("9/2023")
                assertThat(extractTextFromCell(otherHistoryTable.getCell(3, 0))).isEqualTo("2024")
                assertThat(extractTextFromCell(otherHistoryTable.getCell(4, 0))).isEqualTo("Date unknown")
                assertThat(extractTextFromCell(otherHistoryTable.getCell(5, 0))).isEqualTo("Date unknown")
            }
        }
    }

    private fun extractTextFromCell(cell: Cell): String? {
        val paragraph = cell.children.firstOrNull() as? Paragraph
        val textElement = paragraph?.children?.firstOrNull() as? Text
        return textElement?.text
    }

    private fun createTestPriorOtherConditions(): List<PriorOtherCondition> {
        return listOf(
            PriorOtherCondition(
                name = "pancreatitis",
                category = "Pancreas disease",
                isContraindicationForTherapy = true,
                year = null,
                month = null
            ),
            PriorOtherCondition(
                name = "other condition",
                category = "Heart disease",
                isContraindicationForTherapy = true,
                year = 2024,
                month = null
            ),
            PriorOtherCondition(
                name = "other condition",
                category = "Heart disease",
                isContraindicationForTherapy = true,
                year = 2024,
                month = 8
            ),
            PriorOtherCondition(
                name = "other condition",
                category = "Heart disease",
                isContraindicationForTherapy = true,
                year = 2024,
                month = 5
            ),
            PriorOtherCondition(
                name = "other condition",
                category = "Heart disease",
                isContraindicationForTherapy = true,
                year = 2023,
                month = 9
            ), PriorOtherCondition(
                name = "other condition",
                category = "Heart disease",
                isContraindicationForTherapy = true,
                year = null,
                month = 2
            )
        )
    }
}