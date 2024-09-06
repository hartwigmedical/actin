package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.datamodel.clinical.TestPriorOtherConditionFactory
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

        val patientClinicalHistoryGenerator = PatientClinicalHistoryGenerator(reportWithOtherConditions, true, KEY_WIDTH, VALUE_WIDTH)
        val cells = patientClinicalHistoryGenerator.contentsAsList()

        val cell = cells.single { extractTextFromCell(it) == "Relevant non-oncological history" }
        val otherHistoryCell = cells[cells.indexOf(cell) + 1]
        val otherHistoryTable = otherHistoryCell.children.first() as? Table ?: throw IllegalStateException("Expected Table as first child")

        assertThat(otherHistoryTable.numberOfRows).isEqualTo(6)
        assertThat(extractTextFromCell(otherHistoryTable.getCell(0, 0))).isEqualTo("8/2024")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(1, 0))).isEqualTo("5/2024")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(2, 0))).isEqualTo("2024")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(3, 0))).isEqualTo("9/2023")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(4, 0))).isEqualTo("Date unknown")
        assertThat(extractTextFromCell(otherHistoryTable.getCell(5, 0))).isEqualTo("Date unknown")


    }

    private fun extractTextFromCell(cell: Cell): String? {
        val paragraph = cell.children.firstOrNull() as? Paragraph
        val textElement = paragraph?.children?.firstOrNull() as? Text
        return textElement?.text
    }

}