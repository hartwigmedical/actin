package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.efficacy.ConfidenceInterval
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.efficacy.EndPoint
import com.hartwig.actin.datamodel.efficacy.EndPointType
import com.hartwig.actin.datamodel.efficacy.PatientPopulation
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.soc.SoCGeneratorFunctions.analysisGroupForPopulation
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EfficacyEvidenceDetailsGenerator(private val annotation: EfficacyEntry, private val labels: ReportLabels) : TableGenerator {

    override fun title(): String {
        return annotation.acronym
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createSingleCol()
        val patientPopulations = annotation.trialReferences.first().patientPopulations
        if (patientPopulations.size == 2) {
            val subTables = listOf(
                createTrialInformation(),
                createPatientCharacteristics(patientPopulations),
                createEndPointTable(patientPopulations, labels.efficacyEvidence.primaryEndpoints(), EndPointType.PRIMARY),
                createEndPointTable(patientPopulations, labels.efficacyEvidence.secondaryEndpoints(), EndPointType.SECONDARY)
            )
            subTables.forEachIndexed { i, subTable ->
                table.addCell(Cells.create(subTable))
                if (i < subTables.size - 1) {
                    table.addCell(Cells.createEmpty())
                }
            }
        }

        return table
    }

    private fun createTrialInformation(): Table {
        val table = Tables.createFixedWidthCols(100f, 250f).setWidth(350f)
        table.addCell(Cells.createValue(labels.efficacyEvidence.study()))
        table.addCell(
            Cells.createKey(
                listOfNotNull(
                    annotation.acronym,
                    annotation.phase,
                    annotation.therapeuticSetting?.display()
                ).joinToString(", ")
            )
        )
        table.addCell(Cells.createValue(labels.efficacyEvidence.molecularRequirements()))
        if (annotation.variantRequirements.isNotEmpty()) {
            val variantRequirements =
                annotation.variantRequirements.map { variantRequirement ->
                    "${variantRequirement.name} (${variantRequirement.requirementType})"
                }
            table.addCell(Cells.createKey(variantRequirements.joinToString(" and ") { it }))
        } else {
            table.addCell(Cells.createKey(labels.efficacyEvidence.none()))
        }
        table.addCell(Cells.createValue(labels.efficacyEvidence.therapies()))
        table.addCell(Cells.createKey(annotation.treatments.joinToString(", ") { it.display() }))
        table.addCell(Cells.createValue(labels.efficacyEvidence.patientCharacteristics()))
        table.addCell(Cells.createKey(""))
        return table
    }

    private fun contentForCharacteristic(
        characteristic: String, extractAsString: (PatientPopulation) -> String?, patientPopulations: List<PatientPopulation>
    ): List<String> {
        return listOf(characteristic) + (0..1).map { extractAsString(patientPopulations[it]) ?: NA }
    }

    private fun createPatientCharacteristics(patientPopulations: List<PatientPopulation>): Table {
        val table = Tables.createFixedWidthCols(200f, 350f, 350f).setWidth(700f)
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader(patientPopulations[0].name + " (n=" + patientPopulations[0].numberOfPatients + ")"))
        table.addCell(Cells.createHeader(patientPopulations[1].name + " (n=" + patientPopulations[1].numberOfPatients + ")"))

        listOf<Pair<String, (PatientPopulation) -> String?>>(
            labels.efficacyEvidence.colAgeMedianRange() to { "${it.ageMedian} [${it.ageMin}-${it.ageMax}]" },
            labels.efficacyEvidence.colSex() to { "${labels.efficacyEvidence.sexMale()}: ${it.numberOfMale ?: NA}\n ${labels.efficacyEvidence.sexFemale()}: ${it.numberOfFemale}" },
            labels.efficacyEvidence.colRace() to { it.patientsPerRace?.entries?.joinToString(", ") { (key, value) -> "$key: $value patients" } },
            labels.efficacyEvidence.colRegion() to { it.patientsPerRegion?.entries?.joinToString(", ") { (key, value) -> "$key: $value patients" } },
            labels.efficacyEvidence.colWhoEcog() to SoCGeneratorFunctions::createWhoString,
            labels.efficacyEvidence.colPrimaryTumorLocation() to { it.formatTumorLocation("\n") },
            labels.efficacyEvidence.colMutations() to PatientPopulation::mutations,
            labels.efficacyEvidence.colMetastaticSites() to PatientPopulation::formatMetastaticSites,
            labels.efficacyEvidence.colTimeOfMetastases() to { it.timeOfMetastases?.display() },
            labels.efficacyEvidence.colPreviousSystemicTherapy() to { "${it.priorSystemicTherapy ?: NA}/${it.numberOfPatients}" },
            labels.efficacyEvidence.colPriorTherapies() to PatientPopulation::priorTherapies
        )
            .flatMap { (characteristic, extractAsString) ->
                contentForCharacteristic(
                    characteristic,
                    extractAsString,
                    patientPopulations
                )
            }
            .forEach { table.addCell(Cells.createContent(it)) }

        return table
    }

    private fun createEndPointTable(patientPopulations: List<PatientPopulation>, title: String, endPointType: EndPointType): Table {
        val table = Tables.createFixedWidthCols(200f, 140f, 140f, 140f, 80f).setWidth(700f)
        table.addCell(Cells.createValue(title))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createKey(""))
        table.addCell(Cells.createHeader(""))
        table.addCell(Cells.createHeader(patientPopulations[0].name))
        table.addCell(Cells.createHeader(patientPopulations[1].name))
        table.addCell(Cells.createHeader(labels.efficacyEvidence.colHrOr()))
        table.addCell(Cells.createHeader(labels.efficacyEvidence.colPValue()))

        val endPointsById = patientPopulations.flatMap { analysisGroupForPopulation(it)?.endPoints ?: emptyList() }
            .associateBy(EndPoint::id)

        endPointsById.values.filter { endPoint -> endPoint.type == endPointType && endPoint.derivedMetrics.isNotEmpty() }
            .flatMap { endPoint ->
                val otherEndpoint = endPointsById[endPoint.derivedMetrics.first().relativeMetricId]
                val pValue = endPoint.derivedMetrics.first().pValue ?: NA
                listOf(
                    "${endPoint.name} ${labels.efficacyEvidence.ci()}",
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

        table.addCell(Cells.createSpanningSubNote(labels.efficacyEvidence.medianFollowUpPfs(patientPopulations[0].medianFollowUpPFS.toString()), table))

        return table
    }

    private fun formatConfidenceInterval(confidenceInterval: ConfidenceInterval?) =
        "(${confidenceInterval?.lowerLimit ?: NA} - ${confidenceInterval?.upperLimit ?: NA})"
}
