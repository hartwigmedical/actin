package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.efficacy.AnalysisGroup
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
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
        treatments.sortedBy { it.annotations.size }.reversed().forEach { treatment: AnnotatedTreatmentMatch ->
            table.addCell(Cells.createContentBold(treatment.treatmentCandidate.treatment.name))
            if (treatment.annotations.isEmpty()) {
                table.addCell(Cells.createContent("No literature efficacy evidence available yet"))
            } else {
                val subtable = Tables.createFixedWidthCols(50f, 150f).setWidth(200f)
                for (annotation in treatment.annotations) {
                    for (trialReference in annotation.trialReferences) {
                        for (patientPopulation in trialReference.patientPopulations) {
                            if (patientPopulation.treatment?.name.equals(treatment.treatmentCandidate.treatment.name, true)) {
                                val analysisGroup: AnalysisGroup? = if (patientPopulation.analysisGroups.count() == 1) {
                                    patientPopulation.analysisGroups.first()
                                } else {
                                    patientPopulation.analysisGroups.find { it.nPatients == patientPopulation.numberOfPatients } // If there are multiple analysis groups, for now, take analysis group which evaluates all patients, not a subset
                                }
                                subtable.addCell(Cells.createEmpty())
                                subtable.addCell(
                                    Cells.createTitle(annotation.acronym)
                                        .setAction(PdfAction.createURI(annotation.trialReferences.first().url))
                                        .addStyle(Styles.urlStyle())
                                )
                                subtable.addCell(Cells.createValue("PFS: "))
                                if (analysisGroup != null) {
                                    for (primaryEndPoint in analysisGroup.endPoints) {
                                        if (primaryEndPoint.name == "Median Progression-Free Survival") {
                                            if (primaryEndPoint.value != null) {
                                                subtable.addCell(
                                                    Cells.createKey(
                                                        primaryEndPoint.value.toString() + " " + primaryEndPoint.unitOfMeasure.display() + " (95% CI: " + (primaryEndPoint.confidenceInterval?.lowerLimit
                                                            ?: "NA") + "-" + (primaryEndPoint.confidenceInterval?.upperLimit
                                                            ?: "NA") + ")"
                                                    )
                                                )
                                            } else {
                                                subtable.addCell(Cells.createKey("NE"))
                                            }
                                        }
                                    }
                                }

                                subtable.addCell(Cells.createValue("OS: "))
                                if (analysisGroup != null) {
                                    for (primaryEndPoint in analysisGroup.endPoints) {
                                        if (primaryEndPoint.name == "Median Overall Survival") {
                                            if (primaryEndPoint.value != null) {
                                                subtable.addCell(
                                                    Cells.createKey(
                                                        primaryEndPoint.value.toString() + " " + primaryEndPoint.unitOfMeasure.display() + " (95% CI: " + (primaryEndPoint.confidenceInterval?.lowerLimit
                                                            ?: "NA") + "-" + (primaryEndPoint.confidenceInterval?.upperLimit
                                                            ?: "NA") + ")"
                                                    )
                                                )
                                            } else {
                                                subtable.addCell(Cells.createKey("NE"))
                                            }
                                        }
                                    }
                                }
                                subtable.addCell(Cells.createValue(" "))
                                subtable.addCell(Cells.createValue(" "))
                            }
                        }
                    }
                }
                table.addCell(Cells.createContent(subtable))
            }
            table.addCell(Cells.createContent("Not evaluated yet"))
        }
        return table
    }
}