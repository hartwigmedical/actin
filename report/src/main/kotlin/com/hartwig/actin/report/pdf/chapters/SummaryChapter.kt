package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.AggregatedEvidenceInterpreter
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientClinicalHistoryGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleApprovedTreatmentGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleDutchExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleOtherCountriesExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummarizer
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment

class SummaryChapter(private val report: Report, private val externalTrialSummarizer: ExternalTrialSummarizer) : ReportChapter {

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
            "Gender: " to report.patientRecord.patient.gender.display(),
            " | Birth year: " to report.patientRecord.patient.birthYear.toString(),
            " | WHO: " to whoStatus(report.patientRecord.clinicalStatus.who)
        )
        addParagraphWithContent(patientDetailFields, document)

        val (stageTitle, stages) = stageSummary(report.patientRecord.tumor)
        val tumorDetailFields = listOf(
            "Tumor: " to tumor(report.patientRecord.tumor),
            " | Lesions: " to lesions(report.patientRecord.tumor),
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

    private fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    private fun addSummaryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth
        val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch)

        val (openCohortsWithSlots, evaluated) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth(), slotsAvailable = true)
        val (openCohortsWithoutSlots, _) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth(), slotsAvailable = false)

        val molecular = report.patientRecord.molecularHistory.latestOrangeMolecularRecord()
        val (dutchTrialGenerator, nonDutchTrialGenerator) = externalTrials(molecular, evaluated)
        val generators = listOfNotNull(
            PatientClinicalHistoryGenerator(report.patientRecord, report.config, false, keyWidth, valueWidth),
            if (report.config.showMolecularSummary)
                molecular?.let { MolecularSummaryGenerator(report.patientRecord, it, cohorts, keyWidth, valueWidth) } else null,
            if (report.config.showApprovedTreatmentsInSummary)
                EligibleApprovedTreatmentGenerator(report.patientRecord, contentWidth()) else null,
            openCohortsWithSlots,
            openCohortsWithoutSlots,
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

    private fun externalTrials(molecular: MolecularRecord?, evaluated: List<EvaluatedCohort>): Pair<TableGenerator?, TableGenerator?> {
        if (molecular == null) {
            return Pair(null, null)
        } else {
            val externalEligibleTrials = AggregatedEvidenceInterpreter.filterAndGroupExternalTrialsByNctIdAndEvents(
                AggregatedEvidenceFactory.create(molecular).externalEligibleTrialsPerEvent, report.treatmentMatch.trialMatches
            )
            val externalTrialSummary = externalTrialSummarizer.summarize(externalEligibleTrials, evaluated)
            return Pair(
                if (externalTrialSummary.dutchTrials.isNotEmpty()) {
                    EligibleDutchExternalTrialsGenerator(
                        molecular.externalTrialSource,
                        externalTrialSummary.dutchTrials,
                        contentWidth(),
                        externalTrialSummary.dutchTrialsFiltered
                    )
                } else null,
                if (externalTrialSummary.otherCountryTrials.isNotEmpty()) {
                    EligibleOtherCountriesExternalTrialsGenerator(
                        molecular.externalTrialSource,
                        externalTrialSummary.otherCountryTrials,
                        contentWidth(),
                        externalTrialSummary.otherCountryTrialsFiltered
                    )
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