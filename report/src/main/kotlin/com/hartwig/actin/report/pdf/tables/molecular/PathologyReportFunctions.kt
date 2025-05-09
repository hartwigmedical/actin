package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.layout.Style
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import java.time.LocalDate

object PathologyReportFunctions {

    val PathologyReport.date: LocalDate
        get() = requireNotNull(
            tissueDate ?: authorisationDate ?: reportDate
        ) { "Expected one of tissueDate, authorisationDate or reportDate to be non-null." }

    fun getPathologyReportSummary(
        prefix: String? = null,
        prefixStyle: Style? = null,
        pathologyReport: PathologyReport
    ): Cell {
        return Cells.create(
            Paragraph().addAll(
                listOfNotNull(
                    prefix?.let {
                        listOf(
                            Text(prefix).addStyle(prefixStyle),
                            Text(" - ").addStyle(Styles.tableHighlightStyle())
                        )
                    },
                    listOf(
                        Text(pathologyReport.tissueId?.uppercase() ?: "Unknown Tissue ID").addStyle(Styles.tableTitleStyle()),
                        Text(" ($pathologyReport.lab").addStyle(Styles.tableHighlightStyle())
                    ),
                    pathologyReport.tissueDate?.let {
                        getTextWithLabel(", Collection date: ", pathologyReport.tissueDate)
                    },
                    pathologyReport.authorisationDate?.let {
                        getTextWithLabel(", Authorization date: ", pathologyReport.authorisationDate)
                    },
                    pathologyReport.reportDate?.let {
                        getTextWithLabel(", Report date: ", pathologyReport.reportDate)
                    },
                    listOf(
                        Text(", Diagnosis: "),
                        Text(pathologyReport.diagnosis).addStyle(Styles.tableHighlightStyle()),
                        Text(")")
                    )
                ).flatten()
            )
        ).addStyle(Styles.tableContentStyle())
    }

    private fun getTextWithLabel(label: String, date: LocalDate?) =
        listOf(
            Text(label),
            Text(date(date)).addStyle(Styles.tableHighlightStyle())
        )

    fun groupTestsByPathologyReport(
        orangeMolecularRecords: List<MolecularRecord>,
        molecularTests: List<MolecularTest>,
        ihcTests: List<IhcTest>,
        pathologyReports: List<PathologyReport>?
    ): Map<PathologyReport?, Triple<List<MolecularRecord>, List<MolecularTest>, List<IhcTest>>> {

        val reportDates = pathologyReports.orEmpty().map { it.date }.toSet()
        val orangeResultsByDate = orangeMolecularRecords.groupBy { it.date.takeIf(reportDates::contains) }
        val molecularTestsByDate = molecularTests.groupBy { it.date.takeIf(reportDates::contains) }
        val ihcTestsByDate = ihcTests.groupBy { it.measureDate.takeIf(reportDates::contains) }

        val matchedReports: Map<PathologyReport, Triple<List<MolecularRecord>, List<MolecularTest>, List<IhcTest>>> =
            pathologyReports.orEmpty().associateWith { report ->
                Triple(
                    orangeResultsByDate[report.date].orEmpty(),
                    molecularTestsByDate[report.date].orEmpty(),
                    ihcTestsByDate[report.date].orEmpty()
                )
            }

        val unmatchedEntry = Triple(
            orangeResultsByDate[null].orEmpty(),
            molecularTestsByDate[null].orEmpty(),
            ihcTestsByDate[null].orEmpty()
        )
            .takeIf { it.toList().any { e -> e.isNotEmpty() } }
            ?.let { null to it }

        return (matchedReports + listOfNotNull(unmatchedEntry))
            .filterValues { (orangeTests, molecularTest, ihcTests) ->
                orangeTests.isNotEmpty() || molecularTest.isNotEmpty() || ihcTests.isNotEmpty()
            }
            .toList()
            .sortedWith(compareBy(nullsLast(reverseOrder())) { it.first?.date })
            .toMap(LinkedHashMap())
    }
}