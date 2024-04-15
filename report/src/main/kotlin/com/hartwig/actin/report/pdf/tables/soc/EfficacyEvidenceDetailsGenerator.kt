package com.hartwig.actin.report.pdf.tables.soc

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
        val patientPopulations = annotation.trialReferences.first().patientPopulations //currently always 1
        val subTables = listOf(
            createTrialInformation(),
            createPatientCharacteristics(patientPopulations),
            createPrimaryEndpoints(patientPopulations),
            createSecondaryEndpoints(patientPopulations)
        )
        subTables.forEachIndexed { i, subTable ->
            table.addCell(Cells.create(subTable))
            if (i < subTables.size - 1) {
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
        val table = Tables.createFixedWidthCols(200f, 200f, 200f).setWidth(600f)
        // Assuming max 2 treatments per trial
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader(patientPopulations[0].name + " (n=" + patientPopulations[0].numberOfPatients + ")"))
        table.addCell(Cells.createHeader(patientPopulations[1].name + " (n=" + patientPopulations[1].numberOfPatients + ")"))

        table.addCell(Cells.createContent("Age (median [range])"))
        table.addCell(Cells.createContent(patientPopulations[0].ageMedian.toString() + " [" + patientPopulations[0].ageMin + "-" + patientPopulations[0].ageMax + "]"))
        table.addCell(Cells.createContent(patientPopulations[1].ageMedian.toString() + " [" + patientPopulations[1].ageMin + "-" + patientPopulations[1].ageMax + "]"))

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

        table.addCell(Cells.createContent("Race"))
        table.addCell(Cells.createContent(patientPopulations[0].patientsPerRace?.entries?.joinToString(", ") { it.key + ": " + it.value + " patients" }
            ?: "NA"))
        table.addCell(Cells.createContent(patientPopulations[1].patientsPerRace?.entries?.joinToString(", ") { it.key + ": " + it.value + " patients" }
            ?: "NA"))

        table.addCell(Cells.createContent("Region"))
        table.addCell(Cells.createContent(patientPopulations[0].patientsPerRegion?.entries?.joinToString(", ") { it.key + ": " + it.value + " patients" }
            ?: "NA"))
        table.addCell(Cells.createContent(patientPopulations[1].patientsPerRegion?.entries?.joinToString(", ") { it.key + ": " + it.value + " patients" }
            ?: "NA"))

        table.addCell(Cells.createContent("WHO/ECOG"))
        table.addCell(Cells.createContent(createWhoString(patientPopulations[0])))
        table.addCell(Cells.createContent(createWhoString(patientPopulations[1])))

        table.addCell(Cells.createContent("Primary tumor location"))
        table.addCell(Cells.createContent(patientPopulations[0].patientsPerPrimaryTumorLocation?.entries?.joinToString("\n") { "${it.key.replaceFirstChar { word -> word.uppercase() }}: ${it.value}" }
            ?: "NA"))
        table.addCell(Cells.createContent(patientPopulations[1].patientsPerPrimaryTumorLocation?.entries?.joinToString("\n") { "${it.key.replaceFirstChar { word -> word.uppercase() }}: ${it.value}" }
            ?: "NA"))

        table.addCell(Cells.createContent("Mutations"))
        table.addCell(Cells.createContent(patientPopulations[0].mutations ?: "NA"))
        table.addCell(Cells.createContent(patientPopulations[1].mutations ?: "NA"))

        table.addCell(Cells.createContent("Metastatic sites"))
        table.addCell(Cells.createContent(patientPopulations[0].patientsPerMetastaticSites?.entries?.joinToString(", ") { it.key + ": " + it.value.value + " (" + it.value.percentage + "%)" }
            ?: "NA"))
        table.addCell(Cells.createContent(patientPopulations[1].patientsPerMetastaticSites?.entries?.joinToString(", ") { it.key + ": " + it.value.value + " (" + it.value.percentage + "%)" }
            ?: "NA"))

        table.addCell(Cells.createContent("Time of metastases"))
        table.addCell(Cells.createContent(patientPopulations[0].timeOfMetastases?.display() ?: "NA"))
        table.addCell(Cells.createContent(patientPopulations[1].timeOfMetastases?.display() ?: "NA"))

        table.addCell(Cells.createContent("Previous systemic therapy"))
        table.addCell(Cells.createContent((patientPopulations[0].priorSystemicTherapy ?: "NA") + "/" + patientPopulations[0].numberOfPatients))
        table.addCell(Cells.createContent((patientPopulations[1].priorSystemicTherapy ?: "NA") + "/" + patientPopulations[1].numberOfPatients))

        table.addCell(Cells.createContent("Prior therapies"))
        table.addCell(Cells.createContent(patientPopulations[0].priorTherapies ?: "NA"))
        table.addCell(Cells.createContent(patientPopulations[1].priorTherapies ?: "NA"))

        return table
    }

    private fun createPrimaryEndpoints(patientPopulations: List<PatientPopulation>): Table {
        val table = Tables.createFixedWidthCols(200f, 100f, 100f, 100f, 100f).setWidth(600f)
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
                        primaryEndpoints.add(endPoint)
                }
            }
        }
        table.addCell(Cells.createValue("Primary endpoints: "))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader(patientPopulations[0].name))
        table.addCell(Cells.createHeader(patientPopulations[1].name))
        table.addCell(Cells.createHeader("Hazard ratio (HR) / Odds Ratio (OR)"))
        table.addCell(Cells.createHeader("P value"))

        for (endPoint in primaryEndpoints) {
            if (endPoint.type == EndPointType.PRIMARY && endPoint.derivedMetrics.isNotEmpty()) {
                table.addCell(Cells.createContent("${endPoint.name} (95% CI)"))
                table.addCell(Cells.createContent("${endPoint.value} (${endPoint.confidenceInterval?.lowerLimit ?: "NA"} - ${endPoint.confidenceInterval?.upperLimit ?: "NA"})"))
                val otherEndpoint = primaryEndpoints.find { it.id == endPoint.derivedMetrics.first().relativeMetricId }
                table.addCell(Cells.createContent("${otherEndpoint?.value} ${endPoint.unitOfMeasure.display()} (${otherEndpoint?.confidenceInterval?.lowerLimit ?: "NA"} - ${otherEndpoint?.confidenceInterval?.upperLimit ?: "NA"})"))
                table.addCell(Cells.createContent("${endPoint.derivedMetrics.first().value} (${endPoint.derivedMetrics.first().confidenceInterval?.lowerLimit ?: "NA"} - ${endPoint.derivedMetrics.first().confidenceInterval?.upperLimit ?: "NA"})"))
                table.addCell(Cells.createContent("p = ${endPoint.derivedMetrics.first().pValue}"))
            }
        }
        table.addCell(Cells.createSpanningSubNote("Median follow-up for PFS was ${patientPopulations[0].medianFollowUpPFS} months", table))

        return table
    }

    private fun createSecondaryEndpoints(patientPopulations: List<PatientPopulation>): Table {
        val table = Tables.createFixedWidthCols(200f, 100f, 100f, 100f, 100f).setWidth(600f)
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
                    primaryEndpoints.add(endPoint)
                }
            }
        }
        table.addCell(Cells.createValue("Secondary endpoints: "))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader(patientPopulations[0].name))
        table.addCell(Cells.createHeader(patientPopulations[1].name))
        table.addCell(Cells.createHeader("Hazard ratio (HR) / Odds Ratio (OR)"))
        table.addCell(Cells.createHeader("P value"))

        for (endPoint in primaryEndpoints) {
            if (endPoint.type == EndPointType.SECONDARY && endPoint.derivedMetrics.isNotEmpty()) {
                table.addCell(Cells.createContent("${endPoint.name} (95% CI)"))
                table.addCell(Cells.createContent("${endPoint.value} (${endPoint.confidenceInterval?.lowerLimit ?: "NA"} - ${endPoint.confidenceInterval?.upperLimit ?: "NA"})"))
                val otherEndpoint = primaryEndpoints.find { it.id == endPoint.derivedMetrics.first().relativeMetricId }
                table.addCell(Cells.createContent("${otherEndpoint?.value} ${endPoint.unitOfMeasure.display()} (${otherEndpoint?.confidenceInterval?.lowerLimit ?: "NA"} - ${otherEndpoint?.confidenceInterval?.upperLimit ?: "NA"})"))
                table.addCell(Cells.createContent("${endPoint.derivedMetrics.first().value} (${endPoint.derivedMetrics.first().confidenceInterval?.lowerLimit ?: "NA"} - ${endPoint.derivedMetrics.first().confidenceInterval?.upperLimit ?: "NA"})"))
                table.addCell(Cells.createContent("p = ${endPoint.derivedMetrics.first().pValue}"))
            }
        }
        table.addCell(Cells.createSpanningSubNote("Median follow-up for PFS was ${patientPopulations[0].medianFollowUpPFS} months", table))

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