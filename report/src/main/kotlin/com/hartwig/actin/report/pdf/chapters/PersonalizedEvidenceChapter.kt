package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.soc.RealWorldSurvivalOutcomesGenerator
import com.hartwig.actin.report.pdf.tables.soc.RealWorldTreatmentDecisionsGenerator
import com.hartwig.actin.report.pdf.tables.soc.SurvivalPredictionPerTreatmentGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AbstractElement
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Table
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.export.ggsave
import com.itextpdf.layout.element.IElement
import com.itextpdf.svg.converter.SvgConverter
import com.itextpdf.svg.element.SvgImage
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

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


    private fun svgBytesToImage(svgBytes: ByteArray, document: Document): Image {
        val xObj = SvgConverter.convertToXObject(ByteArrayInputStream(svgBytes), document.pdfDocument)
        return Image(xObj)
    }

    private fun gen(document: Document): Image {
        // Example data: multiple lines
        val data = mutableMapOf<String, Any>()
        val x_ = mutableListOf<Double>()
        val y_ = mutableListOf<Double>()
        val group = mutableListOf<String>()
                
        report.treatmentMatch.survivalPredictionsPerTreatment?.map { (treatment, survivalProbability) ->
            survivalProbability.filterIndexed { index, _ -> index % 30 == 0 }.forEachIndexed { index, prob -> 
                x_.add(index.toDouble())
                y_.add(prob)
                group.add(treatment)
            }
        }

        data["x"] = x_
        data["y"] = y_
        data["group"] = group

        val plot = letsPlot(data) { x = x_; y = y_; color = "group" } +
                geomLine() +
                labs(x = "Time (months)", y = "Survival Probability") +
                ggsize(width = 1200, height = 800)


        val tmpFile = File.createTempFile("./out/plot", ".svg")
        // 2. Export plot to SVG in memory
        ggsave(plot, tmpFile.absolutePath)
        val bytes = tmpFile.readBytes()
        return svgBytesToImage(bytes, document)
    }

    private fun addSurvivalTable(document: Document) {
        report.treatmentMatch.survivalPredictionsPerTreatment?.let { survivalPredictions ->
            val table = Tables.createSingleColWithWidth(contentWidth())
            val generator = SurvivalPredictionPerTreatmentGenerator(survivalPredictions)
            TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
            document.add(gen(document))
        }
    }
}

