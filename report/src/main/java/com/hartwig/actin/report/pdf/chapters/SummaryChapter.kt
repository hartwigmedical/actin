package com.hartwig.actin.report.pdf.chapters

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.treatment.EligibleExternalTrialsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import org.apache.logging.log4j.util.Strings

class SummaryChapter(private val report: Report) : ReportChapter {
    override fun name(): String {
        return "Summary"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addPatientDetails(document)
        addChapterTitle(document)
        addSummaryTable(document)
    }

    private fun addPatientDetails(document: Document) {
        val patientDetailsLine = Paragraph()
        patientDetailsLine.add(Text("Gender: ").addStyle(Styles.reportHeaderLabelStyle()))
        patientDetailsLine.add(Text(report.clinical().patient().gender().display()).addStyle(Styles.reportHeaderValueStyle()))
        patientDetailsLine.add(Text(" | Birth year: ").addStyle(Styles.reportHeaderLabelStyle()))
        patientDetailsLine.add(Text(report.clinical().patient().birthYear().toString()).addStyle(Styles.reportHeaderValueStyle()))
        patientDetailsLine.add(Text(" | WHO: ").addStyle(Styles.reportHeaderLabelStyle()))
        patientDetailsLine.add(Text(whoStatus(report.clinical().clinicalStatus().who())).addStyle(Styles.reportHeaderValueStyle()))
        document.add(patientDetailsLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT))
        val tumorDetailsLine = Paragraph()
        tumorDetailsLine.add(Text("Tumor: ").addStyle(Styles.reportHeaderLabelStyle()))
        tumorDetailsLine.add(Text(tumor(report.clinical().tumor())).addStyle(Styles.reportHeaderValueStyle()))
        tumorDetailsLine.add(Text(" | Lesions: ").addStyle(Styles.reportHeaderLabelStyle()))
        tumorDetailsLine.add(Text(lesions(report.clinical().tumor())).addStyle(Styles.reportHeaderValueStyle()))
        tumorDetailsLine.add(Text(" | Stage: ").addStyle(Styles.reportHeaderLabelStyle()))
        tumorDetailsLine.add(Text(stage(report.clinical().tumor())).addStyle(Styles.reportHeaderValueStyle()))
        document.add(tumorDetailsLine.setWidth(contentWidth()).setTextAlignment(TextAlignment.RIGHT))
    }

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addSummaryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth
        val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch())
        val aggregatedEvidence = AggregatedEvidenceFactory.create(report.molecular())
        val generators: MutableList<TableGenerator> = Lists.newArrayList(
            PatientClinicalHistoryGenerator(
                report.clinical(), keyWidth, valueWidth
            ),
            MolecularSummaryGenerator(report.clinical(), report.molecular(), cohorts, keyWidth, valueWidth),
            EligibleApprovedTreatmentGenerator(report.clinical(), report.molecular(), contentWidth()),
            EligibleActinTrialsGenerator.Companion.forOpenCohortsWithSlots(cohorts, contentWidth()),
            EligibleActinTrialsGenerator.Companion.forOpenCohortsWithNoSlots(cohorts, contentWidth())
        )
        if (!aggregatedEvidence.externalEligibleTrialsPerEvent().isEmpty) {
            generators.add(
                EligibleExternalTrialsGenerator(
                    report.molecular().externalTrialSource(),
                    aggregatedEvidence.externalEligibleTrialsPerEvent(),
                    keyWidth,
                    valueWidth
                )
            )
        }
        for (i in generators.indices) {
            val generator = generators[i]
            table.addCell(Cells.createTitle(generator.title()))
            table.addCell(Cells.create(generator.contents()))
            if (i < generators.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        document.add(table)
    }

    companion object {
        private fun whoStatus(who: Int?): String {
            return who?.toString() ?: Formats.VALUE_UNKNOWN
        }

        private fun tumor(tumor: TumorDetails): String {
            val location = tumorLocation(tumor)
            val type = tumorType(tumor)
            return if (location == null || type == null) {
                Formats.VALUE_UNKNOWN
            } else {
                location + if (!type.isEmpty()) " - $type" else Strings.EMPTY
            }
        }

        private fun tumorLocation(tumor: TumorDetails): String? {
            val tumorLocation = tumor.primaryTumorLocation()
            if (tumorLocation != null) {
                val tumorSubLocation = tumor.primaryTumorSubLocation()
                return if (tumorSubLocation != null && !tumorSubLocation.isEmpty()) "$tumorLocation ($tumorSubLocation)" else tumorLocation
            }
            return null
        }

        private fun tumorType(tumor: TumorDetails): String? {
            val tumorType = tumor.primaryTumorType()
            if (tumorType != null) {
                val tumorSubType = tumor.primaryTumorSubType()
                return if (tumorSubType != null && !tumorSubType.isEmpty()) tumorSubType else tumorType
            }
            return null
        }

        private fun stage(tumor: TumorDetails): String {
            val stage = tumor.stage()
            return stage?.display() ?: Formats.VALUE_UNKNOWN
        }

        private fun lesions(tumor: TumorDetails): String {
            val lesions: MutableSet<String?> = Sets.newTreeSet()
            if (tumor.hasCnsLesions() != null && tumor.hasCnsLesions()!!) {
                lesions.add("CNS")
            }
            if (tumor.hasBrainLesions() != null && tumor.hasBrainLesions()!!) {
                lesions.add("Brain")
            }
            if (tumor.hasLiverLesions() != null && tumor.hasLiverLesions()!!) {
                lesions.add("Liver")
            }
            if (tumor.hasBoneLesions() != null && tumor.hasBoneLesions()!!) {
                lesions.add("Bone")
            }
            if (tumor.hasLungLesions() != null && tumor.hasLungLesions()!!) {
                lesions.add("Lung")
            }
            if (tumor.otherLesions() != null) {
                lesions.addAll(tumor.otherLesions()!!)
            }
            if (tumor.biopsyLocation() != null) {
                lesions.add(tumor.biopsyLocation())
            }
            return if (lesions.isEmpty()) {
                Formats.VALUE_UNKNOWN
            } else {
                val joiner = Formats.commaJoiner()
                for (lesion in lesions) {
                    joiner.add(lesion)
                }
                joiner.toString()
            }
        }
    }
}