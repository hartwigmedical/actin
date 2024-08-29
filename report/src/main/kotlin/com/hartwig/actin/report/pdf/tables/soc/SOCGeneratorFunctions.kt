package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.efficacy.AnalysisGroup
import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.efficacy.PatientPopulation
import com.hartwig.actin.efficacy.TrialReference
import com.hartwig.actin.personalized.datamodel.MIN_PATIENT_COUNT
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table

const val NA = "NA"

object SOCGeneratorFunctions {
    private val annotatedTreatmentComparator = Comparator.nullsLast(compareByDescending<AnnotatedTreatmentMatch> { it.generalPfs?.value }
        .thenByDescending { it.annotations.size })

    fun addEndPointsToTable(analysisGroup: AnalysisGroup?, endPointName: String, subTable: Table) {
        val primaryEndPoint = analysisGroup?.endPoints?.find { it.name == endPointName }
        if (primaryEndPoint?.value != null) {
            val ciLower = primaryEndPoint.confidenceInterval?.lowerLimit ?: "NA"
            val ciUpper = primaryEndPoint.confidenceInterval?.upperLimit ?: "NA"
            subTable.addCell(
                Cells.createKey(
                    "${primaryEndPoint.value.toString()} ${primaryEndPoint.unitOfMeasure.display()} (95% CI: $ciLower-$ciUpper)"
                )
            )
        } else {
            subTable.addCell(Cells.createKey("NE"))
        }
    }

    fun analysisGroupForPopulation(patientPopulation: PatientPopulation): AnalysisGroup? {
        return if (patientPopulation.analysisGroups.count() == 1) {
            patientPopulation.analysisGroups.first()
        } else {
            // If there are multiple analysis groups, for now, take analysis group which evaluates all patients, not a subset
            patientPopulation.analysisGroups.find { it.nPatients == patientPopulation.numberOfPatients }
        }
    }

    fun createWhoString(patientPopulation: PatientPopulation): String {
        return with(patientPopulation) {
            listOf(
                patientsWithWho0 to "0",
                patientsWithWho0to1 to "0-1",
                patientsWithWho1 to "1",
                patientsWithWho1to2 to "1-2",
                patientsWithWho2 to "2",
                patientsWithWho3 to "3",
                patientsWithWho4 to "4"
            )
                .mapNotNull { (patients, who) -> patients?.let { "$who: $it" } }
                .joinToString(", ")
        }
    }

    fun abbreviate(treatmentName: String): String {
        val replacements = mapOf(
            "+BEVACIZUMAB" to "-B",
            "+PANITUMUMAB" to "-P"
        )
        return replacements.entries.fold(treatmentName) { acc, (key, value) -> acc.replace(key, value) }
    }

    fun approvedTreatmentCells(treatments: List<AnnotatedTreatmentMatch>): List<Cell> {
        return treatments.sortedWith(annotatedTreatmentComparator)
            .flatMap { treatment: AnnotatedTreatmentMatch ->
                val nameCell = Cells.createContentBold(abbreviate(treatment.treatmentCandidate.treatment.name))

                val annotationsCell = if (treatment.annotations.isEmpty()) {
                    Cells.createContent("Not available yet")
                } else {
                    val subTable = Tables.createFixedWidthCols(25f, 150f).setWidth(175f)
                    treatment.annotations.forEach { annotation -> addTreatmentAnnotationToTable(annotation, treatment, subTable) }
                    Cells.createContent(subTable)
                }

                val warningMessages = treatment.evaluations.flatMap {
                    it.undeterminedGeneralMessages + it.warnGeneralMessages + if (it.recoverable) it.failGeneralMessages else emptyList()
                }
                val warningsCell = Cells.createContent(
                    warningMessages.sorted().distinct().joinToString(Formats.COMMA_SEPARATOR)
                )
                val pfsCell = pfsCell(treatment)

                sequenceOf(nameCell, annotationsCell, pfsCell, warningsCell)
            }
    }

    fun pfsCell(treatment: AnnotatedTreatmentMatch): Cell {
        return Cells.createContent(
            treatment.generalPfs?.run {
                if (numPatients <= MIN_PATIENT_COUNT) NA else {
                    val iqrString = if (iqr != null && iqr != Double.NaN) {
                        ", IQR: $iqr"
                    } else ""
                    value.toString() + iqrString
                }
            } ?: NA
        )
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