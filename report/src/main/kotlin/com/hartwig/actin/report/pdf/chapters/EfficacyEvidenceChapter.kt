package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.configuration.EfficacyEvidenceChapterType
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.algo.ShapDetail
import com.hartwig.actin.datamodel.algo.SimilarPatientsSummary
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.MolecularEfficacyDescriptionGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OffLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OnLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.TreatmentRankingGenerator
import com.hartwig.actin.report.pdf.tables.soc.EfficacyEvidenceDetailsGenerator
import com.hartwig.actin.report.pdf.tables.soc.EfficacyEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.soc.ResistanceEvidenceGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.treatment.EvidenceScoringModel
import com.hartwig.actin.treatment.TreatmentRankingModel
import com.hartwig.actin.treatment.createScoringConfig
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.svg.converter.SvgConverter
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.pos.positionDodge
import org.jetbrains.letsPlot.scale.guides
import org.jetbrains.letsPlot.scale.scaleFillManual
import org.jetbrains.letsPlot.scale.scaleYContinuous
import java.io.ByteArrayInputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.readBytes
import kotlin.math.abs
import kotlin.math.sign

class EfficacyEvidenceChapter(private val report: Report, private val configuration: ReportConfiguration) : ReportChapter {

    private val molecularTests = report.patientRecord.molecularTests
    private val treatmentEvidenceRanking = TreatmentRankingModel(EvidenceScoringModel(createScoringConfig())).rank(report.patientRecord)

    private val plotWidth = contentWidth() - 50
    private val plotHeight = contentHeight() - 100

    override fun name(): String {
        return "Efficacy evidence"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4.rotate()
    }

    override fun include(): Boolean {
        return configuration.efficacyEvidenceChapterType != EfficacyEvidenceChapterType.NONE
    }

    override fun render(document: Document) {
        addChapterTitle(document)

        if (includeStandardOfCareEvidence()) {
            addStandardOfCareEfficacyEvidence(document)
            if (configuration.efficacyEvidenceChapterType == EfficacyEvidenceChapterType.COMPLETE) {
                addStandardOfCareEfficacyEvidenceDetails(document)
            }
            addPersonalizedEfficacyPlots(document)
            addStandardOfCareResistanceEvidence(document)
        }
        if (includeMolecularEvidence()) {
            addMolecularEvidenceRanking(document)
            addMolecularEvidence(document)
            // Commented out for now (vs removal) because I want to look at this code later
            // addMolecularEfficacyDescriptions(document)
        }
    }

    private fun includeStandardOfCareEvidence(): Boolean {
        return configuration.efficacyEvidenceChapterType == EfficacyEvidenceChapterType.STANDARD_OF_CARE_ONLY ||
                configuration.efficacyEvidenceChapterType == EfficacyEvidenceChapterType.COMPLETE
    }

    private fun includeMolecularEvidence(): Boolean {
        return configuration.efficacyEvidenceChapterType == EfficacyEvidenceChapterType.MOLECULAR_ONLY ||
                configuration.efficacyEvidenceChapterType == EfficacyEvidenceChapterType.COMPLETE
    }

    private fun addStandardOfCareEfficacyEvidence(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val efficacyEvidenceGenerator = EfficacyEvidenceGenerator(report.treatmentMatch.standardOfCareMatches?.filter { it.eligible() })

        // TODO (KD): Fit in standard structure.
        table.addCell(Cells.createTitle(efficacyEvidenceGenerator.title()))
        table.addCell(
            Cells.createKey(
                "The following standard of care treatment(s) could be an option for this patient. "
                        + "For further details per study see 'SOC literature details' section in extended report."
            )
        )
        table.addCell(Cells.create(efficacyEvidenceGenerator.contents()))
        document.add(table)
    }

    private fun addStandardOfCareEfficacyEvidenceDetails(document: Document) {
        val socMatches = report.treatmentMatch.standardOfCareMatches?.filter(AnnotatedTreatmentMatch::eligible)

        val table = Tables.createSingleColWithWidth(contentWidth())

        val allAnnotations = socMatches?.flatMap { it.annotations } ?: emptyList()
        if (allAnnotations.isNotEmpty()) {
            val generators =
                allAnnotations.distinctBy { it.acronym }.map { annotation -> EfficacyEvidenceDetailsGenerator(annotation = annotation) }
            TableGeneratorFunctions.addGenerators(generators, table, overrideTitleFormatToSubtitle = true)
            document.add(table)
        } else {
            document.add(Paragraph("There are no standard of care treatment options for this patient").addStyle(Styles.tableContentStyle()))
        }
    }

    private fun addStandardOfCareResistanceEvidence(document: Document) {
        val eligibleSocTreatments = report.treatmentMatch.standardOfCareMatches?.filter(AnnotatedTreatmentMatch::eligible)
            ?.toSet() ?: emptySet()

        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = ResistanceEvidenceGenerator(eligibleSocTreatments, contentWidth())
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }

    private fun addPersonalizedEfficacyPlots(document: Document) {
        report.treatmentMatch.personalizedTreatmentSummary?.predictions?.let { predictions ->
            val image = generateSurvivalPlot(predictions.associate { it.treatment to it.survivalProbs }, document)
            document.add(image)
        }
        report.treatmentMatch.personalizedTreatmentSummary?.predictions?.map { prediction ->
            val image = generateShapPlot(prediction.treatment, prediction.shapValues, document)
            document.add(image)
        }
        report.treatmentMatch.personalizedTreatmentSummary?.similarPatientsSummary?.let { similarPatientsSummary ->
            val image = generateTreatmentDistributionPlot(similarPatientsSummary, document)
            document.add(image)
        }
    }

    private fun generateSurvivalPlot(survivalPredictions: Map<String, List<Double>>, document: Document): Image {
        val data = survivalPredictions
            .flatMap { (treatment, probabilities) ->
                probabilities
                    .filterIndexed { index, _ -> index % 30 == 0 }
                    .mapIndexed { index, prob ->
                        Triple(index.toDouble(), prob, treatment)
                    }
            }

        val survivalTime = data.map { it.first }
        val survivalProbability = data.map { it.second }
        val group = data.map { it.third }

        val plot = letsPlot { x = survivalTime; y = survivalProbability; color = group } +
                geomLine() +
                labs(x = "Time (months)", y = "Survival Probability") +
                ggsize(width = plotWidth, height = plotHeight)

        val tmpFile = createTempFile("plot", ".svg")
        ggsave(plot, tmpFile.absolutePathString())
        val xObj = SvgConverter.convertToXObject(ByteArrayInputStream(tmpFile.readBytes()), document.pdfDocument)
        return Image(xObj).setWidth(plotWidth).setHeight(plotHeight)
    }

    private fun generateShapPlot(treatmentName: String, shapDetails: Map<String, ShapDetail>, document: Document): Image {
        val sortedShapData = shapDetails.toList().sortedByDescending { abs(it.second.shapValue) }.take(10)

        val features = sortedShapData.map { it.first }
        val shapValues = sortedShapData.map { it.second.shapValue }
        val featureValues = sortedShapData.map { it.second.featureValue }
        val yLabels = features.zip(featureValues) { feature, value -> "$feature = %.2f".format(value) }

        val plot = letsPlot { x = shapValues; y = yLabels; fill = shapValues.map { sign(it) } } +
                geomBar(stat = Stat.identity) +
                scaleFillManual(
                    values = mapOf(
                        -1.0 to "blue",
                        0.0 to "white",
                        1.0 to "red",
                    )
                ) +
                guides(fill = "none") +
                ggtitle("SHAP values for treatment: $treatmentName") +
                ggsize(width = plotWidth, height = plotHeight)

        val tmpFile = createTempFile("shap_plot", ".svg")
        ggsave(plot, tmpFile.absolutePathString())
        val xObj = SvgConverter.convertToXObject(ByteArrayInputStream(tmpFile.readBytes()), document.pdfDocument)
        return Image(xObj).setWidth(plotWidth).setHeight(plotHeight)
    }

    private fun generateTreatmentDistributionPlot(similarPatientsSummary: SimilarPatientsSummary, document: Document): Image {
        val categories = similarPatientsSummary.overallTreatmentProportion.map { it.treatment }

        val overallProportion = similarPatientsSummary.overallTreatmentProportion.map { it.proportion }
        val similarPatientsProportion = similarPatientsSummary.similarPatientsTreatmentProportion.map { it.proportion }

        val data = mapOf(
            "treatment" to (categories + categories),
            "proportion" to (overallProportion + similarPatientsProportion),
            "group" to (List(categories.size) { "Overall" } + List(categories.size) { "Similar Patients" })
        )

        val plot = letsPlot(data) +
                geomBar(stat = Stat.identity, position = positionDodge(width = 0.8)) {
                    x = "treatment"
                    y = "proportion"
                    fill = "group"
                } +
                ggtitle("Treatment Distribution in 25 Most Similar Patients vs Overall Population") +
                scaleYContinuous(limits = 0.0 to 1.0) +
                ggsize(width = plotWidth, height = plotHeight)

        val tmpFile = createTempFile("treatment_distribution_plot", ".svg")
        ggsave(plot, tmpFile.absolutePathString())
        val xObj = SvgConverter.convertToXObject(ByteArrayInputStream(tmpFile.readBytes()), document.pdfDocument)
        return Image(xObj).setWidth(plotWidth).setHeight(plotHeight)
    }

    private fun addMolecularEvidence(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val onLabelGenerator = OnLabelMolecularClinicalEvidenceGenerator(molecularTests)
        val offLabelGenerator = OffLabelMolecularClinicalEvidenceGenerator(molecularTests)
        TableGeneratorFunctions.addGenerators(listOf(onLabelGenerator, offLabelGenerator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }

    private fun addMolecularEfficacyDescriptions(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = MolecularEfficacyDescriptionGenerator(molecularTests)
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }

    private fun addMolecularEvidenceRanking(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = TreatmentRankingGenerator(treatmentEvidenceRanking)
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }
}