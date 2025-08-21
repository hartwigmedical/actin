package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.MolecularEfficacyDescriptionGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OffLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.OnLabelMolecularClinicalEvidenceGenerator
import com.hartwig.actin.report.pdf.tables.molecular.TreatmentRankingGenerator
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.treatment.TreatmentEvidenceRanking
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class MolecularEvidenceChapter(
    val report: Report,
    private val treatmentEvidenceRanking: TreatmentEvidenceRanking,
    override val include: Boolean
) : ReportChapter {

    private val molecularTests = report.patientRecord.molecularTests

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
        if (report.config.includeTreatmentEvidenceRanking) addTreatmentEvidenceRankingTable(document)
    }

    private fun addMolecularEvidenceTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val onLabelGenerator = OnLabelMolecularClinicalEvidenceGenerator(molecularTests)
        val offLabelGenerator = OffLabelMolecularClinicalEvidenceGenerator(molecularTests)
        TableGeneratorFunctions.addGenerators(listOf(onLabelGenerator, offLabelGenerator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }

    private fun addEfficacyDescriptionTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = MolecularEfficacyDescriptionGenerator(molecularTests)
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }

    private fun addTreatmentEvidenceRankingTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = TreatmentRankingGenerator(treatmentEvidenceRanking)
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }
}