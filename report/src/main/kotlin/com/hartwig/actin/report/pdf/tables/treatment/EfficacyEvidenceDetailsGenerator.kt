package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.efficacy.AnalysisGroup
import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.efficacy.EndPoint
import com.hartwig.actin.efficacy.EndPointType
import com.hartwig.actin.efficacy.PatientPopulation
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EfficacyEvidenceDetailsGenerator(
    private val annotation: EfficacyEntry,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return annotation.acronym
    }

    override fun contents(): Table {
        val table = Tables.createSingleColWithWidth(width)
        val subtables: MutableList<Table> = mutableListOf()

        subtables.add(createTrialInformation())
        subtables.add(createPatientCharacteristics(annotation.trialReferences.first().patientPopulations))
        subtables.add(createPrimaryEndpoints(annotation.trialReferences.first().patientPopulations))
        subtables.add(createSecondaryEndpoints())

        for (i in subtables.indices) {
            val subtable = subtables[i]
            table.addCell(Cells.create(subtable))
            if (i < subtables.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        return table
    }

    private fun createTrialInformation(): Table {
        val table = Tables.createFixedWidthCols(100f, 250f).setWidth(350f)
        table.addCell(Cells.createValue("Study: "))
        table.addCell(Cells.createKey(annotation.acronym + ", " + annotation.phase + ", " + annotation.therapeuticSetting?.display()))
        table.addCell(Cells.createValue("Molecular requirements: "))
        if (annotation.variantRequirements.isNotEmpty()) {
            val variantRequirements =
                annotation.variantRequirements.map { variantRequirement -> variantRequirement.name + " (" + variantRequirement.requirementType + ")" }
            table.addCell(Cells.createKey(variantRequirements.joinToString(" and ") { it }))
        } else {
            table.addCell(Cells.createKey("None"))
        }
        table.addCell(Cells.createValue("Patient characteristics: "))
        table.addCell(Cells.createKey(""))
        return table
    }

    private fun createPatientCharacteristics(patientPopulations: List<PatientPopulation>): Table {
        val table = Tables.createFixedWidthCols(150f, 100f, 100f, 100f).setWidth(450f)
        // Assuming max 3 treatments per trial
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader(patientPopulations[0].name))
        table.addCell(Cells.createHeader(patientPopulations[1].name))
        if (patientPopulations.size > 2) {
            table.addCell(Cells.createHeader(patientPopulations[2].name))
        } else (table.addCell(Cells.createContent("")))
        table.addCell(Cells.createContent("Age (median [range])"))
        table.addCell(Cells.createContent(patientPopulations[0].ageMedian.toString() + " [" + patientPopulations[0].ageMin + "-" + patientPopulations[0].ageMax + "]"))
        table.addCell(Cells.createContent(patientPopulations[1].ageMedian.toString() + " [" + patientPopulations[1].ageMin + "-" + patientPopulations[1].ageMax + "]"))
        if (patientPopulations.size > 2) {
            table.addCell(Cells.createHeader(patientPopulations[2].ageMedian.toString() + " [" + patientPopulations[2].ageMin + "-" + patientPopulations[2].ageMax + "]"))
        } else table.addCell(Cells.createContent(""))
        table.addCell(Cells.createContent("Sex"))
        table.addCell(
            Cells.createContent(
                "Male: " + (patientPopulations[0].numberOfMale ?: "NA") + "\n Female: " + (patientPopulations[0].numberOfFemale ?: "NA")
            )
        )
        table.addCell(
            Cells.createContent(
                "Male: " + (patientPopulations[1].numberOfMale ?: "NA") + "\n Female: " + (patientPopulations[1].numberOfFemale ?: "NA")
            )
        )
        if (patientPopulations.size > 2) {
            table.addCell(
                Cells.createContent(
                    "Male: " + (patientPopulations[2].numberOfMale ?: "NA") + "\n Female: " + (patientPopulations[2].numberOfFemale ?: "NA")
                )
            )
        } else table.addCell(Cells.createContent(""))
        table.addCell(Cells.createContent("WHO/ECOG"))
        table.addCell(Cells.createContent(createWhoString(patientPopulations[0])))
        table.addCell(Cells.createContent(createWhoString(patientPopulations[1])))
        if (patientPopulations.size > 2) {
            table.addCell(Cells.createContent(createWhoString(patientPopulations[2])))
        } else table.addCell(Cells.createContent(""))
        return table
    }

    private fun createPrimaryEndpoints(patientPopulations: List<PatientPopulation>): Table {
        val table = Tables.createFixedWidthCols(100f, 100f, 100f, 100f, 100f).setWidth(500f)
        val primaryEndpoints = mutableListOf<EndPoint>()
        for (patientPopulation in patientPopulations) {
            val analysisGroup: AnalysisGroup? = if (patientPopulation.analysisGroups.count() == 1) {
                patientPopulation.analysisGroups.first()
            } else {
                patientPopulation.analysisGroups.find { it.nPatients == patientPopulation.numberOfPatients } // If there are multiple analysis groups, for now, take analysis group which evaluates all patients, not a subset
            }
            val endPoints = analysisGroup?.endPoints
            if (endPoints != null) {
                for (endPoint in endPoints) {
                    if (endPoint.type == EndPointType.PRIMARY) {
                        primaryEndpoints.add(endPoint)
                    }
                }
            }
        }
        table.addCell(Cells.createValue("Primary endpoints: "))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader("FOLFOXORI + bevacizumab"))
        table.addCell(Cells.createHeader("FOLFIRI + bevacizumab"))
        table.addCell(Cells.createHeader("Hazard ratio (HR) / Odds Ratio (OR)"))
        table.addCell(Cells.createHeader("P value"))
        table.addCell(Cells.createContent("Time to second progression (95% CI)"))
        table.addCell(Cells.createContent("19.2 months (17.3-21.4)"))
        table.addCell(Cells.createContent("16.4 months (15.1-17.5)"))
        table.addCell(Cells.createContent("HR 0.74 (0.63-0.88)"))
        table.addCell(Cells.createContent("p = 0.0005"))
        return table
    }

    private fun createSecondaryEndpoints(): Table {
        val table = Tables.createFixedWidthCols(100f, 100f, 100f, 100f, 100f).setWidth(500f)
        table.addCell(Cells.createValue("Secondary endpoints: "))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader("FOLFOXORI + bevacizumab"))
        table.addCell(Cells.createHeader("FOLFIRI + bevacizumab"))
        table.addCell(Cells.createHeader("Hazard ratio (HR) / Odds Ratio (OR)"))
        table.addCell(Cells.createHeader("P value"))
        table.addCell(Cells.createContent("Median PFS (95% CI)"))
        table.addCell(Cells.createContent("12 months (11.1-12.9)"))
        table.addCell(Cells.createContent("10 months (9.2-11.6)"))
        table.addCell(Cells.createContent("HR 0.75 (0.63-0.88)"))
        table.addCell(Cells.createContent("p = 0.0005"))
        table.addCell(Cells.createSpanningSubNote("Median follow-up was 35.9 months", table))
        return table
    }

    companion object {
        private fun createWhoString(patientPopulation: PatientPopulation): String {
            val strings = mutableSetOf<String>()
            with(patientPopulation)
            {
                patientsWithWho0?.let { strings.add("0: $it") }
                patientsWithWho0to1?.let { strings.add("0-1: $it") }
                patientsWithWho1?.let { strings.add("1: $it") }
                patientsWithWho1to2?.let { strings.add("1-2: $it") }
                patientsWithWho2?.let { strings.add("2: $it") }
                patientsWithWho3?.let { strings.add("3: $it") }
                patientsWithWho4?.let { strings.add("4: $it") }
            }
            return strings.joinToString(", ")
        }
    }
}