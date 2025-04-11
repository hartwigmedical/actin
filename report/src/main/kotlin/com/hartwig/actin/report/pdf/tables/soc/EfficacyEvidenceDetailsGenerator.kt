package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.efficacy.ConfidenceInterval
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.efficacy.EndPoint
import com.hartwig.actin.datamodel.efficacy.EndPointType
import com.hartwig.actin.datamodel.efficacy.PatientPopulation
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.soc.SOCGeneratorFunctions.analysisGroupForPopulation
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EfficacyEvidenceDetailsGenerator(private val annotation: EfficacyEntry, private val width: Float) : TableGenerator {

    override fun title(): String {
        return annotation.acronym
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createSingleColWithWidth(width)
        val patientPopulations = annotation.trialReferences.first().patientPopulations //currently always 1
        val subTables = listOf(
            createTrialInformation(),
            createPatientCharacteristics(patientPopulations),
            createEndPointTable(patientPopulations, "Primary endpoints: ", EndPointType.PRIMARY),
            createEndPointTable(patientPopulations, "Secondary endpoints: ", EndPointType.SECONDARY)
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
                annotation.variantRequirements.map { variantRequirement ->
                    "${variantRequirement.name} (${variantRequirement.requirementType})"
                }
            table.addCell(Cells.createKey(variantRequirements.joinToString(" and ") { it }))
        } else {
            table.addCell(Cells.createKey("None"))
        }
        table.addCell(Cells.createValue("Therapies: "))
        table.addCell(Cells.createKey(annotation.treatments.joinToString(", ") { it.name }))
        table.addCell(Cells.createValue("Patient characteristics: "))
        table.addCell(Cells.createKey(""))
        return table
    }

    private fun contentForCharacteristic(
        characteristic: String, extractAsString: (PatientPopulation) -> String?, patientPopulations: List<PatientPopulation>
    ): List<String> {
        return listOf(characteristic) + (0..1).map { extractAsString(patientPopulations[it]) ?: NA }
    }

    private fun createPatientCharacteristics(patientPopulations: List<PatientPopulation>): Table {
        val table = Tables.createFixedWidthCols(200f, 200f, 200f).setWidth(600f)
        // Assuming max 2 treatments per trial
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader(patientPopulations[0].name + " (n=" + patientPopulations[0].numberOfPatients + ")"))
        table.addCell(Cells.createHeader(patientPopulations[1].name + " (n=" + patientPopulations[1].numberOfPatients + ")"))

        listOf<Pair<String, (PatientPopulation) -> String?>>(
            "Age (median [range])" to { "${it.ageMedian} [${it.ageMin}-${it.ageMax}]" },
            "Sex" to { "Male: ${it.numberOfMale ?: NA}\n Female: ${it.numberOfFemale}" },
            "Race" to { it.patientsPerRace?.entries?.joinToString(", ") { (key, value) -> "$key: $value patients" } },
            "Region" to { it.patientsPerRegion?.entries?.joinToString(", ") { (key, value) -> "$key: $value patients" } },
            "WHO/ECOG" to SOCGeneratorFunctions::createWhoString,
            "Primary tumor location" to { it.formatTumorLocation("\n") },
            "Mutations" to PatientPopulation::mutations,
            "Metastatic sites" to PatientPopulation::formatMetastaticSites,
            "Time of metastases" to { it.timeOfMetastases?.display() },
            "Previous systemic therapy" to { "${it.priorSystemicTherapy ?: NA}/${it.numberOfPatients}" },
            "Prior therapies" to PatientPopulation::priorTherapies
        )
            .flatMap { (characteristic, extractAsString) -> contentForCharacteristic(characteristic, extractAsString, patientPopulations) }
            .forEach { table.addCell(Cells.createContent(it)) }

        return table
    }

    private fun createEndPointTable(
        patientPopulations: List<PatientPopulation>, title: String, endPointType: EndPointType
    ): Table {
        val table = Tables.createFixedWidthCols(200f, 100f, 100f, 100f, 100f).setWidth(600f)
        table.addCell(Cells.createValue(title))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader(patientPopulations[0].name))
        table.addCell(Cells.createHeader(patientPopulations[1].name))
        table.addCell(Cells.createHeader("Hazard ratio (HR) / Odds Ratio (OR)"))
        table.addCell(Cells.createHeader("P value"))

        val endPointsById = patientPopulations.flatMap { analysisGroupForPopulation(it)?.endPoints ?: emptyList() }
            .associateBy(EndPoint::id)

        endPointsById.values.filter { endPoint -> endPoint.type == endPointType && endPoint.derivedMetrics.isNotEmpty() }
            .flatMap { endPoint ->
                val otherEndpoint = endPointsById[endPoint.derivedMetrics.first().relativeMetricId]
                val pValue = endPoint.derivedMetrics.first().pValue ?: NA
                listOf(
                    "${endPoint.name} (95% CI)",
                    "${endPoint.value} ${formatConfidenceInterval(endPoint.confidenceInterval)}",
                    "${otherEndpoint?.value} ${endPoint.unitOfMeasure.display()} " +
                            formatConfidenceInterval(otherEndpoint?.confidenceInterval),
                    "${endPoint.derivedMetrics.first().value} " +
                            formatConfidenceInterval(endPoint.derivedMetrics.first().confidenceInterval),
                    if (pValue.startsWith("<")) {
                        "p $pValue"
                    } else {
                        "p = $pValue"
                    }
                )
            }
            .forEach { table.addCell(Cells.createContent(it)) }

        table.addCell(Cells.createSpanningSubNote("Median follow-up for PFS was ${patientPopulations[0].medianFollowUpPFS} months", table))

        return table
    }

    private fun formatConfidenceInterval(confidenceInterval: ConfidenceInterval?) =
        "(${confidenceInterval?.lowerLimit ?: NA} - ${confidenceInterval?.upperLimit ?: NA})"
}