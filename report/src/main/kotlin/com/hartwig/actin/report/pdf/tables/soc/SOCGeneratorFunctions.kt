package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.efficacy.AnalysisGroup
import com.hartwig.actin.efficacy.PatientPopulation
import com.hartwig.actin.report.pdf.util.Cells
import com.itextpdf.layout.element.Table

const val NA = "NA"

object SOCGeneratorFunctions {

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
}