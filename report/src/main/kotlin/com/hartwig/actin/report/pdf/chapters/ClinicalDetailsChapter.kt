package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.configuration.ClinicalChapterType
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.ClinicalSummaryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class ClinicalDetailsChapter(private val report: Report, private val configuration: ReportConfiguration) : ReportChapter {

    override fun name(): String {
        return "Clinical Details"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun include(): Boolean {
        return configuration.clinicalChapterType != ClinicalChapterType.NONE
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addClinicalDetails(document)
    }

    private fun addClinicalDetails(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        TableGeneratorFunctions.addGenerators(createClinicalDetailGenerators(), table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }

    fun createClinicalDetailGenerators(): List<TableGenerator> {
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth
        
        return listOfNotNull(
            ClinicalSummaryGenerator(report = report, showDetails = true, keyWidth = keyWidth, valueWidth = valueWidth),
            PatientCurrentDetailsGenerator(
                record = report.patientRecord,
                keyWidth = keyWidth,
                valueWidth = valueWidth,
                referenceDate = report.treatmentMatch.referenceDate
            ),
            TumorDetailsGenerator(record = report.patientRecord, keyWidth = keyWidth, valueWidth = valueWidth),
            report.patientRecord.medications?.let {
                MedicationGenerator(
                    medications = it,
                    interpreter = MedicationStatusInterpreterOnEvaluationDate(report.treatmentMatch.referenceDate, null)
                )
            },
            if (report.patientRecord.bloodTransfusions.isEmpty()) null else
                BloodTransfusionGenerator(bloodTransfusions = report.patientRecord.bloodTransfusions)
        )
    }
}