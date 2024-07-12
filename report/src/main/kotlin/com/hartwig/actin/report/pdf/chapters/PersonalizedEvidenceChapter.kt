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
            "‘All’ shows treatment decisions in NCR patients that were diagnosed with colorectal cancer with distant metastases, " +
                    "treated systemically without surgeries, for whom the treatment could be categorized in above treatments. Patients " +
                    "were previously untreated. This table only shows treatments that are considered SOC.",
            "‘Age’, ‘WHO’, ‘RAS’ and ‘Lesions’ show treatment decisions in the ‘All’ population, plus some patient-specific " +
                    "filtering, as shown in the column name.\n",
            "‘PFS’ is calculated as the date on which the first compound of the treatment was administered, until first progression. " +
                    "When patient count (n<=20) is too low to predict PFS, \"NA\" is shown."
        )
            .map(Cells::createContentNoBorder)
            .forEach(table::addCell)

        document.add(table)
    }
}