package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.MolecularEfficacyDescriptionGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OffLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OnLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.TreatmentRankingGenerator
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class MolecularEvidenceChapter(val report: Report, override val include: Boolean) : ReportChapter {

    private val molecularHistory = report.patientRecord.molecularHistory
    private val ranking = report.treatmentMatch.treatmentEvidenceRanking

    override fun name(): String {
        return "Molecular Evidence"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addMolecularEvidenceTable(document)
        addEfficacyDescriptionTable(document)
        if (report.config.includeTreatmentEvidenceRanking) addTreatmentRankingTable(document)
    }

    private fun addMolecularEvidenceTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val onLabelGenerator = OnLabelMolecularClinicalEvidenceGenerator(molecularHistory)
        val offLabelGenerator = OffLabelMolecularClinicalEvidenceGenerator(molecularHistory)
        TableGeneratorFunctions.addGenerators(listOf(onLabelGenerator, offLabelGenerator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }

    private fun addEfficacyDescriptionTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = MolecularEfficacyDescriptionGenerator(molecularHistory)
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }

    private fun addTreatmentRankingTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = TreatmentRankingGenerator(ranking, contentWidth())
        addGenerators(listOf(generator), table, true)
        document.add(table)
    }
}