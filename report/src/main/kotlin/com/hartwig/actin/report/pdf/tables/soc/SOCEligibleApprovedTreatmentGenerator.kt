package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.efficacy.TrialReference
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.soc.SOCGeneratorFunctions.addEndPointsToTable
import com.hartwig.actin.report.pdf.tables.soc.SOCGeneratorFunctions.analysisGroupForPopulation
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class SOCEligibleApprovedTreatmentGenerator(
    private val report: Report,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return "Standard of care options considered potentially eligible"
    }

    override fun contents(): Table {
        val treatments = report.treatmentMatch.standardOfCareMatches?.filter(AnnotatedTreatmentMatch::eligible)
        if (treatments.isNullOrEmpty()) {
            return Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        }
        val table = Tables.createFixedWidthCols(1f, 1f, 1f).setWidth(width)
        table.addHeaderCell(Cells.createHeader("Treatment"))
        table.addHeaderCell(Cells.createHeader("Literature efficacy evidence"))
        table.addHeaderCell(Cells.createHeader("Personalized PFS prediction"))
        treatments.sortedByDescending { it.annotations.size }.forEach { treatment: AnnotatedTreatmentMatch ->
            table.addCell(Cells.createContentBold(treatment.treatmentCandidate.treatment.name))
            if (treatment.annotations.isEmpty()) {
                table.addCell(Cells.createContent("No literature efficacy evidence available yet"))
            } else {
                val subTable = Tables.createFixedWidthCols(50f, 150f).setWidth(200f)
                treatment.annotations.forEach { annotation -> addTreatmentAnnotationToTable(annotation, treatment, subTable) }
                table.addCell(Cells.createContent(subTable))
            }
            table.addCell(Cells.createContent("Not evaluated yet"))
        }
        return table
    }

    private fun addTreatmentAnnotationToTable(annotation: EfficacyEntry, treatment: AnnotatedTreatmentMatch, subTable: Table) {
        annotation.trialReferences.flatMap(TrialReference::patientPopulations)
            .filter { it.treatment?.name.equals(treatment.treatmentCandidate.treatment.name, true) }
            .forEach { patientPopulation ->
                val analysisGroup = analysisGroupForPopulation(patientPopulation)
                subTable.addCell(Cells.createEmpty())
                subTable.addCell(
                    Cells.createTitle(annotation.acronym)
                        .setAction(PdfAction.createURI(annotation.trialReferences.first().url))
                        .addStyle(Styles.urlStyle())
                )
                subTable.addCell(Cells.createValue("PFS: "))
                addEndPointsToTable(analysisGroup, "Median Progression-Free Survival", subTable)

                subTable.addCell(Cells.createValue("OS: "))
                addEndPointsToTable(analysisGroup, "Median Overall Survival", subTable)

                subTable.addCell(Cells.createValue(" "))
                subTable.addCell(Cells.createValue(" "))
            }
    }
}