package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.layout.Style
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import java.time.LocalDate

data class MolecularTestGroup(
    val records: List<MolecularTest>,
    val tests: List<MolecularTest>,
    val ihc: List<IhcTest>
) {
    val containsOnlyIhcTests: Boolean
        get() = ihc.isNotEmpty() && tests.isEmpty() && records.isEmpty()
}

object PathologyReportFunctions {

    val PathologyReport.date: LocalDate
        get() = requireNotNull(
            tissueDate ?: authorisationDate ?: reportDate
        ) { "Expected one of tissueDate, authorisationDate or reportDate to be non-null." }

    fun createPathologyReportSummaryCell(
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
                        Text(" ("),
                    ),
                    listOfNotNull(
                        pathologyReport.lab?.takeIf { it.isNotBlank() }?.let {
                            listOf(Text(it).addStyle(Styles.tableHighlightStyle()))
                        },
                        pathologyReport.tissueDate?.let {
                            getTextWithLabel("Tissue date: ", pathologyReport.tissueDate)
                        },
                        pathologyReport.authorisationDate?.let {
                            getTextWithLabel("Authorization date: ", pathologyReport.authorisationDate)
                        },
                        pathologyReport.reportDate?.let {
                            getTextWithLabel("Report date: ", pathologyReport.reportDate)
                        },
                        pathologyReport.extractionDate?.let {
                            getTextWithLabel("Data retrieval date: ", pathologyReport.extractionDate)
                        },
                        pathologyReport.diagnosis?.takeIf { it.isNotBlank() }?.let {
                            listOf(
                                Text("Diagnosis: "),
                                Text(pathologyReport.diagnosis).addStyle(Styles.tableHighlightStyle()),
                            )
                        }
                    ).joinWithSeparator(Text(", ")),
                    listOf(Text(")"))
                ).flatten()
            )
        ).addStyle(Styles.tableContentStyle())
    }

    private fun List<List<Text>>.joinWithSeparator(separator: Text): List<Text> =
        reduceOrNull { acc, list -> acc + separator + list } ?: emptyList()

    private fun getTextWithLabel(label: String, date: LocalDate?) =
        listOf(
            Text(label),
            Text(date(date)).addStyle(Styles.tableHighlightStyle())
        )

    fun groupTestsByPathologyReport(
        orangeMolecularRecords: List<MolecularTest>,
        molecularTests: List<MolecularTest>,
        ihcTests: List<IhcTest>,
        pathologyReports: List<PathologyReport>?
    ): Map<PathologyReport?, MolecularTestGroup> {
        val reportKeys = pathologyReports.orEmpty().mapNotNull { it.reportHash }.toSet()
        val orangeResultsByKey = orangeMolecularRecords.groupBy { findReportKey(reportKeys) }
        val molecularTestsByKey = molecularTests.groupBy { findReportKey(reportKeys, it.reportHash) }
        val ihcTestsByKey = ihcTests.groupBy { findReportKey(reportKeys, it.reportHash) }

        val matchedReports: Map<PathologyReport, MolecularTestGroup> =
            pathologyReports.orEmpty().groupBy { it.date }.entries.flatMap { (date, reports) ->
                val sortedReports = reports.sortedBy(PathologyReport::report)
                val firstReport = sortedReports.first()
                val firstReportKeys = listOfNotNull(date.toString(), firstReport.reportHash)
                listOf(firstReport to resultsForKeys(orangeResultsByKey, molecularTestsByKey, ihcTestsByKey, firstReportKeys)) +
                        sortedReports.drop(1).map { report ->
                            Pair(
                                report,
                                report.reportHash?.let { reportHash ->
                                    resultsForKeys(orangeResultsByKey, molecularTestsByKey, ihcTestsByKey, listOf(reportHash))
                                } ?: MolecularTestGroup(emptyList(), emptyList(), emptyList()))
                        }
            }.toMap()

        val unmatchedEntry = MolecularTestGroup(
            orangeResultsByKey[null].orEmpty(), molecularTestsByKey[null].orEmpty(), ihcTestsByKey[null].orEmpty()
        )
            .takeIf { listOf(it.records, it.tests, it.ihc).any { e -> e.isNotEmpty() } }
            ?.let { null to it }

        return (matchedReports + listOfNotNull(unmatchedEntry))
            .filterValues { (orangeTests, molecularTest, ihcTests) ->
                orangeTests.isNotEmpty() || molecularTest.isNotEmpty() || ihcTests.isNotEmpty()
            }
            .toList()
            .sortedWith(
                compareBy<Pair<PathologyReport?, MolecularTestGroup>> { it.second.containsOnlyIhcTests }
                    .thenBy { it.first == null }
                    .thenBy(nullsLast(reverseOrder())) { it.first?.date }
            )
            .toMap(LinkedHashMap())
    }

    private fun findReportKey(reportKeys: Set<String>, reportHash: String? = null): String? = reportHash?.takeIf { reportKeys.contains(it) }

    private fun resultsForKeys(
        orangeResultsByKey: Map<String?, List<MolecularTest>>,
        molecularTestsByKey: Map<String?, List<MolecularTest>>,
        ihcTestsByKey: Map<String?, List<IhcTest>>,
        keys: List<String>
    ) = MolecularTestGroup(
        keys.flatMap { orangeResultsByKey[it].orEmpty() },
        keys.flatMap { molecularTestsByKey[it].orEmpty() },
        keys.flatMap { ihcTestsByKey[it].orEmpty() },
    )
}