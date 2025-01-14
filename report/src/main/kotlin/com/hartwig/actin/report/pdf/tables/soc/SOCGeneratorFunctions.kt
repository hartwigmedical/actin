package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.efficacy.AnalysisGroup
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.efficacy.PatientPopulation
import com.hartwig.actin.datamodel.efficacy.TrialReference
import com.hartwig.actin.datamodel.personalization.MIN_PATIENT_COUNT
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
            val ciLower = primaryEndPoint.confidenceInterval?.lowerLimit ?: NA
            val ciUpper = primaryEndPoint.confidenceInterval?.upperLimit ?: NA
            subTable.addCell(
                Cells.createKey(
                    "${primaryEndPoint.value.toString()} ${primaryEndPoint.unitOfMeasure.display()} (95% CI: $ciLower-$ciUpper)"
                )
            )
        } else {
            subTable.addCell(Cells.createKey(NA))
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

                val efficacyEvidenceCell = addRealWorldEfficacyToTable(treatment)

                val warningMessages = treatment.evaluations.flatMap {
                    it.undeterminedMessages + it.warnMessages + if (it.recoverable) it.failMessages else emptyList()
                }
                val warningsCell = Cells.createContent(
                    warningMessages.sorted().distinct().joinToString(Formats.COMMA_SEPARATOR)
                )
                sequenceOf(nameCell, annotationsCell, efficacyEvidenceCell, warningsCell)
            }
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

    private fun addRealWorldEfficacyToTable(treatment: AnnotatedTreatmentMatch): Cell {
        val subTable = Tables.createFixedWidthCols(25f, 150f).setWidth(175f)

        val efficacyDataList = listOf(
            "PFS" to treatment.generalPfs,
            "OS" to treatment.generalOs
        )

        if (efficacyDataList.all { (_, data) -> data == null || data.numPatients <= MIN_PATIENT_COUNT }) {
            return Cells.createContent("Not available yet")
        }

        if (treatment.annotations.isNotEmpty()) {
            subTable.addCell(Cells.createValue("\n"))
            subTable.addCell(Cells.createKey("\n"))
        }

        for ((name, data) in efficacyDataList) {
            val value = data?.takeIf { it.numPatients > MIN_PATIENT_COUNT }?.let {
                val iqrString = if (it.iqr != null && !it.iqr!!.isNaN()) {
                    ", IQR: ${Formats.daysToMonths(it.iqr!!)}"
                } else ""
                "${Formats.daysToMonths(it.value)} months$iqrString"
            } ?: NA
            subTable.addCell(Cells.createValue("$name: "))
            subTable.addCell(Cells.createKey(value))
        }

        return Cells.createContent(subTable)
    }
}