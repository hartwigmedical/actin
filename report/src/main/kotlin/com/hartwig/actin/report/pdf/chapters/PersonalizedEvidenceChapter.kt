package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.algo.TreatmentEfficacyPrediction
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.soc.RealWorldSurvivalOutcomesGenerator
import com.hartwig.actin.report.pdf.tables.soc.RealWorldTreatmentDecisionsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Table
import com.itextpdf.svg.converter.SvgConverter
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleFillGradient2
import java.io.ByteArrayInputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.readBytes
import kotlin.math.abs


class PersonalizedEvidenceChapter(private val report: Report, override val include: Boolean) : ReportChapter {

    override fun name(): String {
        return "SOC personalized real-world evidence annotation"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)

        addPersonalizationTable(document)
        addSurvivalTable(document)
    }

    private fun addPersonalizationTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())

        val eligibleSocTreatments = report.treatmentMatch.standardOfCareMatches
            ?.filter(AnnotatedTreatmentMatch::eligible)
            ?.map { it.treatmentCandidate.treatment.name.lowercase() }
            ?.toSet() ?: emptySet()

        val personalizedDataAnalysis = report.treatmentMatch.personalizedDataAnalysis!!

        val generators = listOfNotNull(
            RealWorldTreatmentDecisionsGenerator(personalizedDataAnalysis, eligibleSocTreatments, contentWidth()),
            RealWorldSurvivalOutcomesGenerator(
                personalizedDataAnalysis,
                eligibleSocTreatments,
                contentWidth(),
                MeasurementType.OVERALL_SURVIVAL
            ),
            RealWorldSurvivalOutcomesGenerator(
                personalizedDataAnalysis,
                eligibleSocTreatments,
                contentWidth(),
                MeasurementType.PROGRESSION_FREE_SURVIVAL
            ),
        )

        generators.forEach { generator ->
            val groupingTable = Table(1).setKeepTogether(true).setPadding(0f)

            groupingTable.addCell(Cells.createSubTitle(generator.title()))
            groupingTable.addCell(Cells.create(generator.contents()))

            table.addCell(Cells.create(groupingTable))
        }

        table.addCell(Cells.createSubTitle("Explanation:"))
        sequenceOf(
            "These tables only show treatments that are considered standard of care (SOC) in colorectal cancer in the Netherlands.\n",
            "The ‘All’ column shows results in NCR patients who were previously untreated, diagnosed with colorectal cancer with distant " +
                    "metastases and treated systemically without surgery, for whom the treatment could be categorized in SOC treatments.\n",
            "The ‘Age’, ‘WHO’, ‘RAS’ and ‘Lesions’ columns show results based on patients from the ‘All’ population, filtered " +
                    "for equal WHO, similar age, equal RAS status or equal lesion localization, respectively.\n",
            "‘PFS’ is calculated as the duration from the date on which the first compound of the treatment was administered, until first progression. ",
            "‘OS’ is calculated as the duration from the date on which the first compound of the treatment was administered, until death from any cause.\n",
            "When patient number is too low (n <= 20) to predict PFS or OS, \"NA\" is shown.\n",
        )
            .map(Cells::createContentNoBorder)
            .forEach(table::addCell)

        document.add(table)
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
                ggsize(width = 1500, height = 800)

        val tmpFile = createTempFile("plot", ".svg")
        ggsave(plot, tmpFile.absolutePathString())
        val xObj = SvgConverter.convertToXObject(ByteArrayInputStream(tmpFile.readBytes()), document.pdfDocument)
        return Image(xObj)
    }

    private fun generateShapPlot(
        treatmentName: String,
        shapDetails: Map<String, TreatmentEfficacyPrediction.ShapDetail>,
        document: Document
    ): Image {
        val sortedShapData = shapDetails.toList().sortedByDescending { abs(it.second.shapValue) }.take(10)

        val features = sortedShapData.map { it.first }
        val shapValues = sortedShapData.map { it.second.shapValue }
        val featureValues = sortedShapData.map { it.second.featureValue }
        val yLabels = features.zip(featureValues) { feature, value -> "$feature = %.2f".format(value) }

        val plot = letsPlot { x = shapValues; y = yLabels; fill = shapValues } +
                geomBar(
                    stat = Stat.identity,
                ) +
                scaleFillGradient2(
                    low = "blue",    // negative values
                    mid = "white",   // zero
                    high = "red",    // positive values
                    midpoint = 0.0
                ) +
                ggtitle("SHAP values for treatment: $treatmentName") +
                ggsize(width = 1500, height = 800)

        val tmpFile = createTempFile("shap_plot", ".svg")
        ggsave(plot, tmpFile.absolutePathString())
        val xObj = SvgConverter.convertToXObject(ByteArrayInputStream(tmpFile.readBytes()), document.pdfDocument)
        return Image(xObj)
    }

    private fun addSurvivalTable(document: Document) {
        report.treatmentMatch.survivalPredictionsPerTreatment?.let { survivalPredictions ->
            val image = generateSurvivalPlot(survivalPredictions.mapValues { it.value.survivalProbs }, document)
            document.add(image)
        }
        report.treatmentMatch.survivalPredictionsPerTreatment?.map { (treatment, prediction) ->
            val image = generateShapPlot(treatment, prediction.shapValues, document)
            document.add(image)
        }
    }
}
