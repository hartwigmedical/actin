package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil
import com.itextpdf.layout.Style
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val YEAR = 2023
private const val MONTH = 1

class PathologyReportFunctionsTest {
    private val minimalRecord = TestMolecularFactory.createMinimalWholeGenomeTest()
    private val minimalPanel = TestMolecularFactory.createMinimalPanelTest()
    private val date1 = LocalDate.of(YEAR, MONTH, 1)
    private val date2 = LocalDate.of(YEAR, MONTH, 2)
    private val df = DateTimeFormatter.ofPattern("dd-MMM-yyyy")


    @Test
    fun `Should return complete pathology report summary`() {
        val pathologyReport = pathologyReport(1, date1).copy(authorisationDate = date2, reportDate = date2)

        val cell = PathologyReportFunctions.getPathologyReportSummary(
            prefix = "Test",
            prefixStyle = Style(),
            pathologyReport = pathologyReport
        )

        with(pathologyReport) {
            assertThat(CellTestUtil.extractTextFromCell(cell))
                .isEqualTo(
                    "Test - T-100001 ($lab, Collection date: ${df.format(tissueDate)}, " +
                            "Authorization date: ${df.format(authorisationDate)}, " +
                            "Report date: ${df.format(reportDate)}, Diagnosis: $diagnosis)"
                )
        }
    }

    @Test
    fun `Should return pathology report summary with unknown tissue and report date only`() {
        val pathologyReport = pathologyReport(1, date1).copy(
            tissueId = null,
            authorisationDate = null,
            tissueDate = null,
            reportDate = date1,
            lab = null,
            diagnosis = null,
        )

        val cell = PathologyReportFunctions.getPathologyReportSummary(pathologyReport = pathologyReport)

        with(pathologyReport) {
            assertThat(CellTestUtil.extractTextFromCell(cell))
                .isEqualTo(
                    "Unknown Tissue ID (Report date: ${df.format(reportDate)})"
                )
        }
    }

    @Test
    fun `Should group test results by report hash if match available or date otherwise`() {
        val pathologyReport1 = pathologyReport(1, date1)
        val pathologyReport2 = pathologyReport(2, date2)
        val pathologyReport3 = pathologyReport(3, date2)

        val molecularRecord1 = minimalRecord.copy(date = date1)
        val molecularTest1 = minimalPanel.copy(date = date1)
        val molecularTest2 = minimalPanel.copy(date = date2, reportHash = "hash2")
        val molecularTest3 = minimalPanel.copy(date = date2, reportHash = "hash3")
        val ihcTest1 = ihcTest(measureDate = date1, reportHash = "unknown")
        val ihcTest2 = ihcTest(reportHash = "hash1")
        val ihcTest3 = ihcTest(measureDate = date2, reportHash = "hash2")
        val ihcTest4 = ihcTest(reportHash = "hash3")

        val result = PathologyReportFunctions.groupTestsByPathologyReport(
            orangeMolecularRecords = listOf(molecularRecord1),
            molecularTests = listOf(molecularTest1, molecularTest2, molecularTest3),
            ihcTests = listOf(ihcTest1, ihcTest2, ihcTest3, ihcTest4),
            pathologyReports = listOf(pathologyReport1, pathologyReport2, pathologyReport3)
        )

        assertThat(result[pathologyReport1])
            .isEqualTo(MolecularTestGroup(listOf(molecularRecord1), listOf(molecularTest1), listOf(ihcTest1, ihcTest2)))
        assertThat(result[pathologyReport2])
            .isEqualTo(MolecularTestGroup(emptyList(), listOf(molecularTest2), listOf(ihcTest3)))
        assertThat(result[pathologyReport3])
            .isEqualTo(MolecularTestGroup(emptyList(), listOf(molecularTest3), listOf(ihcTest4)))
    }

    @Test
    fun `Should only show date matches for the first report with same date`() {
        val pathologyReport1 = pathologyReport(1, date1)
        val pathologyReport2 = pathologyReport(2, date1)

        val molecularRecord1 = minimalRecord.copy(date = date1)
        val molecularTest1 = minimalPanel.copy(date = date1)
        val molecularTest2 = minimalPanel.copy(date = date1, reportHash = "hash2")
        val ihcTest1 = ihcTest(measureDate = date1)
        val ihcTest2 = ihcTest(reportHash = "hash1")
        val ihcTest3 = ihcTest(measureDate = date1, reportHash = "hash2")

        val result = PathologyReportFunctions.groupTestsByPathologyReport(
            orangeMolecularRecords = listOf(molecularRecord1),
            molecularTests = listOf(molecularTest1, molecularTest2),
            ihcTests = listOf(ihcTest1, ihcTest2, ihcTest3),
            pathologyReports = listOf(pathologyReport1, pathologyReport2)
        )

        assertThat(result[pathologyReport1])
            .isEqualTo(MolecularTestGroup(listOf(molecularRecord1), listOf(molecularTest1), listOf(ihcTest1, ihcTest2)))
        assertThat(result[pathologyReport2])
            .isEqualTo(MolecularTestGroup(emptyList(), listOf(molecularTest2), listOf(ihcTest3)))
    }

    @Test
    fun `Should group unmatched results under null report`() {
        val pathologyReport1 = pathologyReport(1, date1)

        val molecularRecord1 = minimalRecord.copy(date = date2)
        val molecularTest1 = minimalPanel.copy(date = date2)
        val molecularTest2 = minimalPanel.copy(reportHash = "unknown")
        val ihcTest1 = ihcTest(measureDate = date2, reportHash = "unknown")

        val result = PathologyReportFunctions.groupTestsByPathologyReport(
            orangeMolecularRecords = listOf(molecularRecord1),
            molecularTests = listOf(molecularTest1, molecularTest2),
            ihcTests = listOf(ihcTest1),
            pathologyReports = listOf(pathologyReport1)
        )

        assertThat(result[null])
            .isEqualTo(MolecularTestGroup(listOf(molecularRecord1), listOf(molecularTest1, molecularTest2), listOf(ihcTest1)))
    }

    private fun pathologyReport(idx: Int, date: LocalDate): PathologyReport = PathologyReport(
        reportHash = "hash$idx",
        tissueId = "T-10000$idx",
        lab = "Lab$idx",
        diagnosis = "Diagnosis$idx",
        tissueDate = date,
        authorisationDate = null,
        report = "Report$idx"
    )

    private fun ihcTest(item: String = "", measure: String? = null, measureDate: LocalDate? = null, reportHash: String? = null): IhcTest {
        return IhcTest(item = item, measure = measure, measureDate = measureDate, reportHash = reportHash)
    }
}