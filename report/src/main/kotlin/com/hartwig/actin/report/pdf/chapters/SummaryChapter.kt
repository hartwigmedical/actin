package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment

class SummaryChapter(private val report: Report) : ReportChapter {

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
            "Tumor: " to tumor(report.patientRecord.tumor),
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
        val contentWidth = contentWidth()
        val table = Tables.createSingleColWithWidth(contentWidth)
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth - keyWidth
        val generators = ReportContentProvider(report).provideSummaryTables(keyWidth, valueWidth, contentWidth)

        generators.flatMap { generator ->
            sequenceOf(
                Cells.createTitle(generator.title()),
                Cells.create(generator.contents()),
                Cells.createEmpty(),
                Cells.createEmpty()
            )
        }
            .dropLast(2)
            .forEach(table::addCell)
        document.add(table)
    }

    private fun whoStatus(who: Int?): String {
        return who?.toString() ?: Formats.VALUE_UNKNOWN
    }

    private fun tumor(tumor: TumorDetails): String {
        val location = tumorLocation(tumor)
        val type = tumorType(tumor)
        return if (location == null || type == null) {
            Formats.VALUE_UNKNOWN
        } else {
            location + if (type.isNotEmpty()) " - $type" else ""
        }
    }

    private fun tumorLocation(tumor: TumorDetails): String? {
        return tumor.primaryTumorLocation?.let { tumorLocation ->
            val tumorSubLocation = tumor.primaryTumorSubLocation
            return if (!tumorSubLocation.isNullOrEmpty()) "$tumorLocation ($tumorSubLocation)" else tumorLocation
        }
    }

    private fun tumorType(tumor: TumorDetails): String? {
        return tumor.primaryTumorType?.let { tumorType ->
            val tumorSubType = tumor.primaryTumorSubType
            if (!tumorSubType.isNullOrEmpty()) tumorSubType else tumorType
        }
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