package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.soc.RealWorldPFSOutcomesGenerator
import com.hartwig.actin.report.pdf.tables.soc.RealWorldTreatmentDecisionsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class PersonalizedEvidenceChapter(private val report: Report, override val include: Boolean) : ReportChapter {
    override fun name(): String {
        return "SOC personalized real-world evidence annotation"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        val eligibleSocTreatments = report.treatmentMatch.standardOfCareMatches?.filter(AnnotatedTreatmentMatch::eligible)
            ?.map { it.treatmentCandidate.treatment.name.lowercase() }
            ?.toSet() ?: emptySet()

        addChapterTitle(document)

        val table = Tables.createSingleColWithWidth(contentWidth())
        listOf(
            RealWorldTreatmentDecisionsGenerator(report.treatmentMatch.personalizedDataAnalysis!!, eligibleSocTreatments, contentWidth()),
            RealWorldPFSOutcomesGenerator(report.treatmentMatch.personalizedDataAnalysis!!, eligibleSocTreatments, contentWidth())
        ).flatMap { generator ->
            sequenceOf(
                Cells.createSubTitle(generator.title()),
                Cells.create(generator.contents()),
                Cells.createEmpty()
            )
        }
            .dropLast(1)
            .forEach(table::addCell)

        table.addCell(Cells.createSubTitle("Explanation:"))
        sequenceOf(
            "This table only shows treatments that are considered standard of care (SOC) in colorectal cancer in the Netherlands.\n",
            "‘All’ column shows results in NCR patients who were previously untreated, diagnosed with colorectal cancer with distant " +
                    "metastases and treated systemically without surgery, for whom the treatment could be categorized in SOC treatments.\n",
            "‘Age’, ‘WHO’, ‘RAS’ and ‘Lesions’ columns show results based on patients from the ‘All’ population, plus additional " +
                    "filtering on patients according to equal WHO, similar age, equal RAS status or equal lesion localization.\n",
            "‘PFS’ is calculated as the date on which the first compound of the treatment was administered, until first progression. " +
                    "When patient number is too low (n <= 20) to predict PFS, \"NA\" is shown."
        )
            .map(Cells::createContentNoBorder)
            .forEach(table::addCell)

        document.add(table)
    }
}