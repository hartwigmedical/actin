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
)

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
                        Text(" ("),
                    ),
                    listOfNotNull(
                        pathologyReport.lab?.takeIf { it.isNotBlank() }?.let {
                            listOf(Text(it).addStyle(Styles.tableHighlightStyle()))
                        },
                        pathologyReport.tissueDate?.let {
                            getTextWithLabel("Collection date: ", pathologyReport.tissueDate)
                        },
                        pathologyReport.authorisationDate?.let {
                            getTextWithLabel("Authorization date: ", pathologyReport.authorisationDate)
                        },
                        pathologyReport.reportDate?.let {
                            getTextWithLabel("Report date: ", pathologyReport.reportDate)
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
        val reportKeys = pathologyReports.orEmpty().flatMap { listOfNotNull(it.date.toString(), it.reportHash) }.toSet()
        val orangeResultsByKey = orangeMolecularRecords.groupBy { findReportKey(reportKeys, it.date) }
        val molecularTestsByKey = molecularTests.groupBy { findReportKey(reportKeys, it.date, it.reportHash) }
        val ihcTestsByKey = ihcTests.groupBy { findReportKey(reportKeys, it.measureDate, it.reportHash) }

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
            .sortedWith(compareBy(nullsLast(reverseOrder())) { it.first?.date })
            .toMap(LinkedHashMap())
    }

    private fun findReportKey(reportKeys: Set<String>, date: LocalDate?, reportHash: String? = null): String? =
        listOfNotNull(reportHash, date.toString()).find(reportKeys::contains)

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