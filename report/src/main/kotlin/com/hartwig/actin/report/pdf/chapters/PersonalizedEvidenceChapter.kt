package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.report.pdf.tables.soc.RealWorldSurvivalOutcomesGenerator
import com.hartwig.actin.report.pdf.tables.soc.RealWorldTreatmentDecisionsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.Property

class PersonalizedEvidenceChapter(private val report: Report, override val include: Boolean) : ReportChapter {

    override fun name(): String {
        return "SOC personalized real-world evidence annotation"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        val eligibleSocTreatments = report.treatmentMatch.standardOfCareMatches
            ?.filter(AnnotatedTreatmentMatch::eligible)
            ?.map { it.treatmentCandidate.treatment.name.lowercase() }
            ?.toSet() ?: emptySet()

        addChapterTitle(document)

        val table = Tables.createSingleColWithWidth(contentWidth())

        val generators = listOf(
            RealWorldTreatmentDecisionsGenerator(report.treatmentMatch.personalizedDataAnalysis!!, eligibleSocTreatments, contentWidth()),
            RealWorldSurvivalOutcomesGenerator(report.treatmentMatch.personalizedDataAnalysis!!, eligibleSocTreatments, contentWidth(), MeasurementType.PROGRESSION_FREE_SURVIVAL),
            RealWorldSurvivalOutcomesGenerator(report.treatmentMatch.personalizedDataAnalysis!!, eligibleSocTreatments, contentWidth(), MeasurementType.OVERALL_SURVIVAL)
        )

        generators.forEach { generator ->
            val innerTable = Table(1).apply { setProperty(Property.KEEP_TOGETHER, true) }

            val titleCell = Cells.createSubTitle(generator.title())

            innerTable.addCell(titleCell)
            innerTable.addCell(Cells.create(generator.contents()))

            table.addCell(Cells.create(innerTable))
        }

        table.addCell(Cells.createSubTitle("Explanation:"))
        sequenceOf(
            "These tables only shows treatments that are considered standard of care (SOC) in colorectal cancer in the Netherlands.\n",
            "The ‘All’ column shows results in NCR patients who were previously untreated, diagnosed with colorectal cancer with distant " +
                    "metastases and treated systemically without surgery, for whom the treatment could be categorized in SOC treatments.\n",
            "The ‘Age’, ‘WHO’, ‘RAS’ and ‘Lesions’ columns show results based on patients from the ‘All’ population, filtered " +
                    "for equal WHO, similar age, equal RAS status or equal lesion localization, respectively.\n",
            "‘PFS’ is calculated as the duration from the date on which the first compound of the treatment was administered, until first progression. ",
            "‘OS’ is calculated as the duration from the incidence date until death from any cause.\n",
            "When patient number is too low (n <= 20) to predict PFS or OS, \"NA\" is shown.\n",
        )
            .map(Cells::createContentNoBorder)
            .forEach(table::addCell)

        document.add(table)
    }
}

