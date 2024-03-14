package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.efficacy.AnalysisGroup
import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.efficacy.PatientPopulation
import com.hartwig.actin.efficacy.TrialReference
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EfficacyEvidenceGenerator(
    private val treatments: List<AnnotatedTreatmentMatch>?,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return "Standard of care options considered potentially eligible"
    }

    override fun contents(): Table {
        if (treatments.isNullOrEmpty()) {
            return Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val table = Tables.createFixedWidthCols(100f, width - 250f, 150f).setWidth(width)
            table.addHeaderCell(Cells.createHeader("Treatment"))
            table.addHeaderCell(Cells.createHeader("Literature efficacy evidence"))
            table.addHeaderCell(Cells.createHeader("Database efficacy evidence"))
            treatments.sortedBy { it.treatmentCandidate.treatment.name }.forEach { treatment: AnnotatedTreatmentMatch ->
                table.addCell(Cells.createContentBold(treatment.treatmentCandidate.treatment.name))
                if (treatment.annotations.isNotEmpty()) {
                    val subtable = Tables.createSingleColWithWidth(width / 2)
                    for (annotation in treatment.annotations) {
                        for (trialReference in annotation.trialReferences) {
                            subtable.addCell(Cells.create(createOneLiteraturePart(width, annotation, trialReference, treatment)))
                        }
                    }
                    table.addCell(Cells.createContent(subtable))
                } else table.addCell(Cells.createContent("No literature evidence available yet"))

                table.addCell(Cells.createContent("Not evaluated yet"))
            }
            return table
        }
    }

    private fun createOneLiteraturePart(
        width: Float,
        annotation: EfficacyEntry,
        trialReference: TrialReference,
        treatment: AnnotatedTreatmentMatch
    ): Table {
        val subtable = Tables.createSingleColWithWidth(width / 2)
        val subsubtables: MutableList<Table> = mutableListOf()
        subsubtables.add(createTrialHeader(annotation))
        subsubtables.add(createPatientCharacteristics(trialReference, treatment))
        subsubtables.add(createEndpoints(trialReference, treatment))
        for (i in subsubtables.indices) {
            val subsubtable = subsubtables[i]
            subtable.addCell(Cells.create(subsubtable))
            if (i < subsubtables.size - 1) {
                subtable.addCell(Cells.createEmpty())
            }
        }
        return subtable
    }

    private fun createTrialHeader(annotation: EfficacyEntry): Table {
        val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
        table.addCell(Cells.createSubTitle(annotation.acronym))
        table.addCell(Cells.createValue(""))
        table.addCell(Cells.createValue("Patient characteristics: "))
        table.addCell(Cells.createKey(""))
        return table
    }

    private fun createPatientCharacteristics(trialReference: TrialReference, treatment: AnnotatedTreatmentMatch): Table {
        val table = Tables.createFixedWidthCols(100f, 150f).setWidth(400f)
        for (patientPopulation in trialReference.patientPopulations) {
            if (!patientPopulation.treatment?.name.isNullOrEmpty() && patientPopulation.treatment?.name.equals(
                    treatment.treatmentCandidate.treatment.name,
                    true
                )
            ) {
                table.addCell(Cells.createContent("WHO/ECOG"))
                table.addCell(Cells.createContent(createWhoString(patientPopulation)))
                table.addCell(Cells.createContent("Primary tumor location"))
                table.addCell(Cells.createContent(patientPopulation.patientsPerPrimaryTumorLocation?.entries?.joinToString(", ") { "${it.key.replaceFirstChar { word -> word.uppercase() }}: ${it.value}" }
                    ?: "No information available"))
                table.addCell(Cells.createContent("Mutations"))
                table.addCell(Cells.createContent(patientPopulation.mutations ?: "NA"))
                table.addCell(Cells.createContent("Metastatic sites"))
                table.addCell(Cells.createContent(patientPopulation.patientsPerMetastaticSites?.entries?.joinToString(", ") { it.key + ": " + it.value.value + " (" + it.value.value + "%)" }
                    ?: "No information available"))
                table.addCell(Cells.createContent("Previous systemic therapy"))
                table.addCell(Cells.createContent(patientPopulation.priorSystemicTherapy ?: "No information available"))
                table.addCell(Cells.createContent("Prior therapies"))
                table.addCell(Cells.createContent(patientPopulation.priorTherapies ?: "No information available"))

            }
        }
//        table.addCell(
//            Cells.createSpanningSubNote(
//                "This patient matches all patient characteristics of the treatment group, except for age (68 years)",
//                table
//            )
//        )
        return table
    }

    private fun createWhoString(patientPopulation: PatientPopulation): String {
        val strings = mutableSetOf<String>()
        with(patientPopulation) {
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

    private fun createEndpoints(trialReference: TrialReference, treatment: AnnotatedTreatmentMatch): Table {
        val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
        for (patientPopulation in trialReference.patientPopulations) {
            if (!patientPopulation.treatment?.name.isNullOrEmpty() && patientPopulation.treatment?.name.equals(
                    treatment.treatmentCandidate.treatment.name,
                    true
                )
            ) {
                val analysisGroup: AnalysisGroup? = if (patientPopulation.analysisGroups.count() == 1) {
                    patientPopulation.analysisGroups.first()
                } else {
                    patientPopulation.analysisGroups.find { it.nPatients == patientPopulation.numberOfPatients } // If there are multiple analysis groups, for now, take analysis group which evaluates all patients, not a subset
                }
                table.addCell(Cells.createValue("Median PFS: "))
                if (analysisGroup != null) {
                    for (primaryEndPoint in analysisGroup.endPoints) {
                        if (primaryEndPoint.name == "Median Progression-Free Survival") {
                            if (primaryEndPoint.value != null) {
                                table.addCell(
                                    Cells.createKey(
                                        primaryEndPoint.value.toString() + " " + primaryEndPoint.unitOfMeasure.display() + " (95% CI: " + (primaryEndPoint.confidenceInterval?.lowerLimit
                                            ?: "NA") + "-" + (primaryEndPoint.confidenceInterval?.upperLimit ?: "NA") + ")"
                                    )
                                )
                            } else {
                                table.addCell(Cells.createKey("NE"))
                            }
                        }
                    }
                }

                table.addCell(Cells.createValue("Median OS: "))
                if (analysisGroup != null) {
                    for (primaryEndPoint in analysisGroup.endPoints) {
                        if (primaryEndPoint.name == "Median Overall Survival") {
                            if (primaryEndPoint.value != null) {
                                table.addCell(
                                    Cells.createKey(
                                        primaryEndPoint.value.toString() + " " + primaryEndPoint.unitOfMeasure.display() + " (95% CI: " + (primaryEndPoint.confidenceInterval?.lowerLimit
                                            ?: "NA") + "-" + (primaryEndPoint.confidenceInterval?.upperLimit ?: "NA") + ")"
                                    )
                                )
                            } else {
                                table.addCell(Cells.createKey("NE"))
                            }
                        }
                    }
                }
            }
        }
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createEmpty())
        return table
    }
}