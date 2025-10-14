package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.configuration.ReportContentType
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment

class SummaryChapter(private val report: Report, private val reportContentProvider: ReportContentProvider) : ReportChapter {

    override fun name(): String {
        return "Summary"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        if (report.configuration.patientDetailsType != ReportContentType.NONE) {
            addPatientDetails(document)
        }
        addSummaryTables(document)
    }

    private fun addPatientDetails(document: Document) {
        val patientDetailFields = listOf(
            "Gender: " to (report.patientRecord.patient.gender?.display() ?: Formats.VALUE_UNKNOWN),
            " | Birth year: " to report.patientRecord.patient.birthYear.toString(),
            " | WHO: " to whoStatus(report.patientRecord.performanceStatus.latestWho)
        )
        addParagraphWithContent(document, patientDetailFields)

        val (stageTitle, stages) = stageSummary(report.patientRecord.tumor)
        val tumorDetailFields = listOfNotNull(
            "Tumor: " to report.patientRecord.tumor.name,
            if (report.configuration.patientDetailsType == ReportContentType.COMPREHENSIVE) {
                " | Lesions: " to TumorDetailsInterpreter.lesionString(report.patientRecord.tumor)
            } else null,
            " | $stageTitle: " to stages
        )
        addParagraphWithContent(document, tumorDetailFields)
    }

    private fun whoStatus(who: Int?): String {
        return who?.toString() ?: Formats.VALUE_UNKNOWN
    }

    private fun stageSummary(tumor: TumorDetails): Pair<String, String> {
        val knownStage = "Stage"
        return when {
            tumor.stage != null -> {
                Pair(knownStage, tumor.stage!!.display())
            }

            !tumor.derivedStages.isNullOrEmpty() -> {
                Pair("Derived stage(s)", tumor.derivedStages!!.sorted().joinToString(", ") { it.display() })
            }

            else -> {
                Pair(knownStage, "Unknown")
            }
        }
    }

    private fun addParagraphWithContent(document: Document, contentFields: List<Pair<String, String>>) {
        val paragraph = Paragraph()
        contentFields.flatMap { (label, value) ->
            listOf(
                Text(label).addStyle(Styles.reportHeaderLabelStyle()),
                Text(value).addStyle(Styles.reportHeaderValueStyle())
            )
        }.forEach(paragraph::add)
        document.add(paragraph.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT))
    }

    private fun addSummaryTables(document: Document) {
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth
        val generators = reportContentProvider.provideSummaryTables(keyWidth, valueWidth)

        val table = Tables.createSingleColWithWidth(contentWidth())
        TableGeneratorFunctions.addGenerators(generators, table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }
}