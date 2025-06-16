package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
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

class SummaryChapter(
    private val report: Report,
    private val reportContentProvider: ReportContentProvider,
    private val interpretedCohorts: List<InterpretedCohort>
) : ReportChapter {

    override fun name(): String {
        return "Summary"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        if (report.config.includePatientHeader) {
            addPatientDetails(document)
        }
        addChapterTitle(document)
        addSummaryTable(document)
    }

    private fun addPatientDetails(document: Document) {
        val patientDetailFields = listOf(
            "Gender: " to report.patientRecord.patient.gender.display(),
            " | Birth year: " to report.patientRecord.patient.birthYear.toString(),
            " | WHO: " to whoStatus(report.patientRecord.clinicalStatus.who)
        )
        addParagraphWithContent(patientDetailFields, document)

        val (stageTitle, stages) = stageSummary(report.patientRecord.tumor)
        val tumorDetailFields = listOf(
            "Tumor: " to report.patientRecord.tumor.name,
            " | Lesions: " to TumorDetailsInterpreter.lesions(report.patientRecord.tumor),
            " | $stageTitle: " to stages
        )
        addParagraphWithContent(tumorDetailFields, document)
    }

    private fun addParagraphWithContent(contentFields: List<Pair<String, String>>, document: Document) {
        val paragraph = Paragraph()
        contentFields.flatMap { (label, value) ->
            listOf(
                Text(label).addStyle(Styles.reportHeaderLabelStyle()),
                Text(value).addStyle(Styles.reportHeaderValueStyle())
            )
        }.forEach(paragraph::add)
        document.add(paragraph.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT))
    }

    private fun addSummaryTable(document: Document) {
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth
        val generators = reportContentProvider.provideSummaryTables(keyWidth, valueWidth, interpretedCohorts)

        val table = Tables.createSingleColWithWidth(contentWidth())
        TableGeneratorFunctions.addGenerators(generators, table, overrideTitleFormatToSubtitle = false)
        document.add(table)
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
}