package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IHCTest
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
            if (isSourceInternal) tissueDate else externalDate
        ) { "Expected one of tissueDate or externalDate to be non-null." }

    fun getPathologyReportSummary(prefix: String? = null, prefixStyle: Style? = null, report: PathologyReport): Cell =
        Cells.create(
            Paragraph().addAll(
                listOfNotNull(
                    prefix?.let {
                        listOf(
                            Text(prefix).addStyle(prefixStyle),
                            Text(" - ").addStyle(Styles.tableHighlightStyle())
                        )
                    },
                    listOf(Text(report.tissueId?.uppercase() ?: "Unknown Tissue ID").addStyle(Styles.tableTitleStyle())),
                    if (report.isSourceInternal) {
                        listOf(
                            Text(" (Collection date: "),
                            Text(date(report.tissueDate)).addStyle(Styles.tableHighlightStyle()),
                            Text(", Authorization date: "),
                            Text(date(report.authorisationDate)).addStyle(Styles.tableHighlightStyle())
                        )
                    } else {
                        listOf(
                            Text(" (Report date: "),
                            Text(date(report.externalDate)).addStyle(Styles.tableHighlightStyle())
                        )
                    },
                    listOf(
                        Text(", Diagnosis: "),
                        Text(report.diagnosis).addStyle(Styles.tableHighlightStyle()),
                        Text(")")
                    )
                ).flatten()
            )
        ).addStyle(Styles.tableContentStyle())

    fun groupTestsByPathologyReport(
        orangeMolecularRecords: List<MolecularRecord>,
        molecularTests: List<MolecularTest>,
        ihcTests: List<IHCTest>,
        pathologyReports: List<PathologyReport>?
    ): Map<PathologyReport?, Triple<List<MolecularRecord>, List<MolecularTest>, List<IHCTest>>> {

        val reportDates = pathologyReports.orEmpty().map { it.date }.toSet()
        val orangeResultsByDate = orangeMolecularRecords.groupBy { it.date.takeIf(reportDates::contains) }
        val molecularTestsByDate = molecularTests.groupBy { it.date.takeIf(reportDates::contains) }
        val ihcTestsByDate = ihcTests.groupBy { it.measureDate.takeIf(reportDates::contains) }

        val matchedReports: Map<PathologyReport, Triple<List<MolecularRecord>, List<MolecularTest>, List<IHCTest>>> =
            pathologyReports.orEmpty().associateWith { report ->
                Triple(
                    orangeResultsByDate[report.date] ?: emptyList(),
                    molecularTestsByDate[report.date] ?: emptyList(),
                    ihcTestsByDate[report.date] ?: emptyList()
                )
            }

        val unmatchedEntry = (null to Triple(
            orangeResultsByDate[null] ?: emptyList(),
            molecularTestsByDate[null] ?: emptyList(),
            ihcTestsByDate[null] ?: emptyList()
        ))
            .takeIf { it.second.first.isNotEmpty() || it.second.second.isNotEmpty() || it.second.third.isNotEmpty() }

        return (matchedReports + listOfNotNull(unmatchedEntry))
            .filterValues { (orangeTests, molecularTest, ihcTests) ->
                orangeTests.isNotEmpty() || molecularTest.isNotEmpty() || ihcTests.isNotEmpty()
            }
            .toList()
            .sortedWith(compareBy(nullsLast(reverseOrder())) { it.first?.date })
            .toMap(LinkedHashMap())
    }
}