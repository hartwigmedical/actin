package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.ReportContentType
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.clinical.ClinicalSummaryGenerator
import com.hartwig.actin.report.pdf.tables.molecular.MolecularSummaryGenerator
import com.hartwig.actin.report.pdf.tables.soc.EligibleStandardOfCareGenerator
import com.hartwig.actin.report.pdf.tables.soc.ProxyStandardOfCareGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrials
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment

class SummaryChapter(
    private val report: Report,
    private val configuration: ReportConfiguration,
    private val trialsProvider: TrialsProvider
) : ReportChapter {

    override fun name(): String {
        return "Summary"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun include(): Boolean {
        return true
    }

    override fun render(document: Document) {
        if (configuration.patientDetailsType != ReportContentType.NONE) {
            addPatientDetails(document)
        }
        addSummaryTable(document)
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
            if (configuration.patientDetailsType == ReportContentType.COMPREHENSIVE) {
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

    private fun addSummaryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        TableGeneratorFunctions.addGenerators(createSummaryGenerators(), table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }

    fun createSummaryGenerators(): List<TableGenerator> {
        val keyWidth = Formats.STANDARD_KEY_WIDTH
        val valueWidth = contentWidth() - keyWidth

        val clinicalSummaryGenerator =
            ClinicalSummaryGenerator(
                report = report,
                includeAdditionalFields = configuration.clinicalSummaryType == ReportContentType.COMPREHENSIVE,
                keyWidth = keyWidth,
                valueWidth = valueWidth
            ).takeIf {
                configuration.clinicalSummaryType != ReportContentType.NONE
            }

        val molecularSummaryGenerator = MolecularSummaryGenerator(
            patientRecord = report.patientRecord,
            cohorts = trialsProvider.evaluableCohortsAndNotIgnore(),
            keyWidth = keyWidth,
            valueWidth = valueWidth
        ).takeIf {
            configuration.molecularSummaryType != ReportContentType.NONE
        }

        val standardOfCareTableGenerator = when (configuration.standardOfCareSummaryType) {
            ReportContentType.NONE -> null
            ReportContentType.BRIEF -> ProxyStandardOfCareGenerator(report).takeIf { it.showTable() }
            ReportContentType.COMPREHENSIVE -> EligibleStandardOfCareGenerator(report)
        }

        val trialTableGenerators = createTrialTableGenerators(
            cohorts = trialsProvider.evaluableCohortsAndNotIgnore(),
            externalTrials = trialsProvider.externalTrials(),
            requestingSource = TrialSource.fromDescription(configuration.hospitalOfReference)
        ).takeIf { configuration.trialMatchingSummaryType != ReportContentType.NONE } ?: emptyList()

        return listOfNotNull(
            clinicalSummaryGenerator,
            molecularSummaryGenerator,
            standardOfCareTableGenerator
        ) + trialTableGenerators
    }

    private fun createTrialTableGenerators(
        cohorts: List<InterpretedCohort>,
        externalTrials: ExternalTrials,
        requestingSource: TrialSource?
    ): List<TrialTableGenerator> {
        val localOpenCohortsGenerator =
            EligibleTrialGenerator.localOpenCohorts(
                cohorts = cohorts,
                externalTrials = externalTrials,
                requestingSource = requestingSource,
                countryOfReference = configuration.countryOfReference
            )

        val localOpenCohortsWithMissingMolecularResultForEvaluationGenerator =
            EligibleTrialGenerator.forOpenCohortsWithMissingMolecularResultsForEvaluation(cohorts, requestingSource)

        val nonLocalTrialGenerator = EligibleTrialGenerator.nonLocalOpenCohorts(externalTrials, requestingSource)

        return listOfNotNull(
            localOpenCohortsGenerator,
            localOpenCohortsWithMissingMolecularResultForEvaluationGenerator.takeIf { it?.cohortSize() != 0 },
            nonLocalTrialGenerator.takeIf { externalTrials.internationalTrials.isNotEmpty() },
        )
    }
}