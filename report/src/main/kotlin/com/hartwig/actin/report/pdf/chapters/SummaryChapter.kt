package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.AggregatedEvidenceInterpreter
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleDutchExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleExternalTrialGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.trial.EligibleOtherCountriesExternalTrialsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import java.time.LocalDate

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
        val patientDetailFields = listOf(
            "Gender: " to report.clinical.patient.gender.display(),
            " | Birth year: " to report.clinical.patient.birthYear.toString(),
            " | WHO: " to whoStatus(report.clinical.clinicalStatus.who)
        )
        addParagraphWithContent(patientDetailFields, document)

        val tumorDetailFields = listOf(
            "Tumor: " to tumor(report.clinical.tumor),
            " | Lesions: " to lesions(report.clinical.tumor),
            " | Stage: " to stage(report.clinical.tumor)
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

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addSummaryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth
        val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch)

        val (dutchTrialGenerator, nonDutchTrialGenerator) = externalTrials(report.molecular)

        val generators = listOfNotNull(
            if (report.config.showClinicalSummary) PatientClinicalHistoryGenerator(report.clinical, keyWidth, valueWidth) else null,
            if (report.molecular?.date != null && report.molecular.date!! > LocalDate.now().minusDays(21)) {
                MolecularSummaryGenerator(report.clinical, report.molecular, cohorts, keyWidth, valueWidth)
            } else null,
            EligibleApprovedTreatmentGenerator(report.clinical, report.molecular, contentWidth()),
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth(), slotsAvailable = true),
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth(), slotsAvailable = false),
            dutchTrialGenerator,
            nonDutchTrialGenerator
        )

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

    private fun externalTrials(molecular: MolecularRecord?): Pair<TableGenerator?, TableGenerator?> {
        if (molecular == null) {
            return Pair(null, null)
        } else {
            val externalEligibleTrials = AggregatedEvidenceInterpreter.filterAndGroupExternalTrialsByNctIdAndEvents(
                AggregatedEvidenceFactory.create(molecular).externalEligibleTrialsPerEvent, report.treatmentMatch.trialMatches
            )
            val dutchTrials = EligibleExternalTrialGeneratorFunctions.dutchTrials(externalEligibleTrials)
            val otherTrials = EligibleExternalTrialGeneratorFunctions.nonDutchTrials(externalEligibleTrials)
            return Pair(
                if (dutchTrials.isNotEmpty()) {
                    EligibleDutchExternalTrialsGenerator(molecular.externalTrialSource, dutchTrials, contentWidth())
                } else null,
                if (otherTrials.isNotEmpty()) {
                    EligibleOtherCountriesExternalTrialsGenerator(molecular.externalTrialSource, otherTrials, contentWidth())
                } else null
            )
        }
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

        private fun stage(tumor: TumorDetails): String {
            return tumor.stage?.display() ?: Formats.VALUE_UNKNOWN
        }

        fun lesions(tumor: TumorDetails): String {
            val categorizedLesions = listOf(
                "CNS" to tumor.hasCnsLesions,
                "Brain" to tumor.hasBrainLesions,
                "Liver" to tumor.hasLiverLesions,
                "Bone" to tumor.hasBoneLesions,
                "Lung" to tumor.hasLungLesions
            ).filter { it.second == true }.map { it.first }

            val lesions =
                listOfNotNull(categorizedLesions, tumor.otherLesions, listOfNotNull(tumor.biopsyLocation)).flatten()
                    .sorted().distinctBy { it.uppercase() }

            val (lymphNodeLesions, otherLesions) = lesions.partition { it.lowercase().startsWith("lymph node") }

            val filteredLymphNodeLesions = lymphNodeLesions.map { lesion ->
                lesion.split(" ").filterNot { it.lowercase() in setOf("lymph", "node", "nodes", "") }.joinToString(" ")
            }.filterNot(String::isEmpty).distinctBy(String::lowercase)

            val lymphNodeLesionsString = if (filteredLymphNodeLesions.isNotEmpty()) {
                listOf("Lymph nodes (${filteredLymphNodeLesions.joinToString(", ")})")
            } else if (lymphNodeLesions.isNotEmpty()) {
                listOf("Lymph nodes")
            } else emptyList()

            return if (lesions.isEmpty()) {
                Formats.VALUE_UNKNOWN
            } else {
                (otherLesions + lymphNodeLesionsString).joinToString(", ")
            }
        }
    }
}