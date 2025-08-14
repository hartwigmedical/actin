package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.soc.RealWorldSurvivalOutcomesGenerator
import com.hartwig.actin.report.pdf.tables.soc.RealWorldTreatmentDecisionsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AbstractElement
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Table
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.export.ggsave
import com.itextpdf.svg.converter.SvgConverter
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import java.io.ByteArrayInputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.readBytes


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
    
    private fun generateSurvivalPlot(survivalPredictions:Map<String, List<Double>>, document: Document): Image {
        val survivalTime = mutableListOf<Double>()
        val survivalProbability = mutableListOf<Double>()
        val group = mutableListOf<String>()
                
        survivalPredictions.map { (treatment, survivalProbabilities) ->
            survivalProbabilities.filterIndexed { index, _ -> index % 30 == 0 }.forEachIndexed { index, prob -> 
                survivalTime.add(index.toDouble())
                survivalProbability.add(prob)
                group.add(treatment)
            }
        }
        val plot = letsPlot { x = survivalTime; y = survivalProbability; color = group } +
                geomLine() +
                labs(x = "Time (months)", y = "Survival Probability") +
                ggsize(width = 1500, height = 800)
        
        val tmpFile = createTempFile("plot", ".svg")
        ggsave(plot, tmpFile.absolutePathString())
        val xObj = SvgConverter.convertToXObject(ByteArrayInputStream(tmpFile.readBytes()), document.pdfDocument)
        return Image(xObj)
    }

    private fun addSurvivalTable(document: Document) {
        report.treatmentMatch.survivalPredictionsPerTreatment?.let { survivalPredictions ->
            val image = generateSurvivalPlot(survivalPredictions, document)
            document.add(image)
        }
    }
}

