package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.IHCTest
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

object PathologyReportFunctions {

    val PathologyReport.date: LocalDate
        get() = requireNotNull(
            if (isSourceInternal) tissueDate else externalDate
        ) { "Expected one of tissueDate or externalDate to be non-null." }

    fun getPathologyReport(patientRecord: PatientRecord, date: LocalDate?): PathologyReport? =
        date?.let {
            patientRecord.pathologyReports
                ?.filter { report -> !report.tissueId.isNullOrEmpty() }
                ?.firstOrNull { report ->
                    println(report)
                    report.date == date
                }
        }

    fun getPathologyReportSummary(prefix: String? = null, prefixStyle: Style? = null, report: PathologyReport): Cell =
        Cells.create(
            Paragraph()
                .apply {
                    prefix?.let {
                        add(Text(prefix).addStyle(prefixStyle))
                        add(Text(" - ").addStyle(Styles.tableHighlightStyle()))
                    }
                }
                .add(Text(report.tissueId?.uppercase()).addStyle(Styles.tableTitleStyle()))
                .apply {
                    if (report.isSourceInternal) {
                        add(Text(" (Collection date: ")).add(Text(date(report.tissueDate)).addStyle(Styles.tableHighlightStyle()))
                        add(Text(", Authorization date: ")).add(Text(date(report.authorisationDate)).addStyle(Styles.tableHighlightStyle()))
                    } else {
                        add(Text(" (Report date: ")).add(Text(date(report.externalDate)).addStyle(Styles.tableHighlightStyle()))
                    }
                }
                .add(Text(", Diagnosis: "))
                .add(Text(report.diagnosis).addStyle(Styles.tableHighlightStyle())).add(Text(")"))
        ).addStyle(Styles.tableContentStyle())

    fun groupTestsByPathologyReport(
        molecularTests: List<MolecularTest>,
        ihcTests: List<IHCTest>,
        pathologyReports: List<PathologyReport>?
    ): Map<PathologyReport?, Pair<List<MolecularTest>, List<IHCTest>>> {

        val reportDates = pathologyReports.orEmpty().map { it.date }.toSet()

        val (matchedMolecularTests, unmatchedMolecularTests) = molecularTests.partition { it.date in reportDates }
        val (matchedIhcTests, unmatchedIHCTests) = ihcTests.partition { it.measureDate in reportDates }

        val matchedReports: Map<PathologyReport, Pair<List<MolecularTest>, List<IHCTest>>> =
            pathologyReports.orEmpty().associateWith { report ->
                Pair(
                    matchedMolecularTests.filter { it.date == report.date },
                    matchedIhcTests.filter { it.measureDate == report.date }
                )
            }

        val unmatchedEntry = (null to (unmatchedMolecularTests to unmatchedIHCTests))
            .takeIf { unmatchedMolecularTests.isNotEmpty() || unmatchedIHCTests.isNotEmpty() }

        return (matchedReports + listOfNotNull(unmatchedEntry))
            .toList()
            .sortedWith(compareBy(nullsLast(reverseOrder())) { it.first?.date })
            .toMap(LinkedHashMap())
    }
}