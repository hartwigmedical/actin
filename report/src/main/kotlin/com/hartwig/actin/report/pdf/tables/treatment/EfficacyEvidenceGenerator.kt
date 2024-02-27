package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.efficacy.PatientPopulation
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
            treatments?.forEach { treatment: AnnotatedTreatmentMatch ->
                table.addCell(Cells.createContentBold(treatment.treatmentCandidate.treatment.name))
                val subtable = Tables.createSingleColWithWidth(width / 2)
                if (treatment.annotations.isNotEmpty()) {
                    for (annotation in treatment.annotations) {
                        subtable.addCell(Cells.create(createOneLiteraturePart(width, annotation, treatment)))
                    }
                } else subtable.addCell(Cells.createContent("No literature evidence available yet"))

                table.addCell(Cells.createContent(subtable))

                table.addCell(Cells.createContent("Not evaluated yet"))
            }
            return table
        }
    }

    private fun createOneLiteraturePart(width: Float, annotation: EfficacyEntry, treatment: AnnotatedTreatmentMatch): Table {
        val subtable = Tables.createSingleColWithWidth(width / 2)
        val subsubtables: MutableList<Table> = mutableListOf()
        subsubtables.add(createTrialHeader(annotation))
        subsubtables.add(createPatientCharacteristics(annotation, treatment))
        subsubtables.add(createEndpoints(annotation, treatment))
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

    private fun createPatientCharacteristics(annotation: EfficacyEntry, treatment: AnnotatedTreatmentMatch): Table {
        val table = Tables.createFixedWidthCols(150f, 150f).setWidth(500f)
        for (patientPopulation in annotation.trialReferences.iterator().next().patientPopulations) {
            if (!patientPopulation.therapy.isNullOrEmpty() && patientPopulation.therapy == treatment.treatmentCandidate.treatment.name) {
                table.addCell(Cells.createContent("WHO/ECOG"))
                table.addCell(Cells.createContent(createWhoString(patientPopulation)))
                table.addCell(Cells.createContent("Primary tumor location"))
                table.addCell(Cells.createContent(patientPopulation.patientsPerPrimaryTumorLocation?.entries?.joinToString(", ") { "${it.key}: ${it.value}" }
                    ?: "No information"))
                table.addCell(Cells.createContent("Mutations"))
                table.addCell(Cells.createContent(patientPopulation.mutations ?: "NA"))
                table.addCell(Cells.createContent("Metastatic sites"))
                table.addCell(Cells.createContent(patientPopulation.patientsPerMetastaticSites?.entries?.joinToString(", ") { it.key + ": " + it.value.value + " (" + it.value.value + "%)" }
                    ?: "No information"))
                table.addCell(Cells.createContent("Previous systemic chemotherapy"))
                table.addCell(Cells.createContent(patientPopulation.priorSystemicTherapy.toString()))
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
        return strings.joinToString("\n")
    }

    private fun createEndpoints(annotation: EfficacyEntry, treatment: AnnotatedTreatmentMatch): Table {
        val table = Tables.createFixedWidthCols(100f, 150f).setWidth(250f)
        val paper = annotation.trialReferences.iterator().next() // for now assume we only have 1 paper per trial
        for (patientPopulation in paper.patientPopulations) {
            if (!patientPopulation.therapy.isNullOrEmpty() && patientPopulation.therapy == treatment.treatmentCandidate.treatment.name) {
                val analysisGroup =
                    patientPopulation.analysisGroups.iterator().next() // assume only 1 analysis group per patient population
                table.addCell(Cells.createValue("Median PFS: "))
                for (primaryEndPoint in analysisGroup.primaryEndPoints!!) {
                    if (primaryEndPoint.name == "Median Progression-Free Survival") {
                        table.addCell(
                            Cells.createKey(
                                primaryEndPoint.value.toString() + " " + primaryEndPoint.unitOfMeasure.display() + " (95% CI: " + (primaryEndPoint.confidenceInterval?.lowerLimit
                                    ?: "NA") + "-" + (primaryEndPoint.confidenceInterval?.upperLimit ?: "NA") + ")"
                            )
                        )
                    } else {
                        table.addCell(Cells.createKey("NA"))
                    }
                }

                table.addCell(Cells.createValue("Median OS: "))
                for (primaryEndPoint in analysisGroup.primaryEndPoints!!) {
                    if (primaryEndPoint.name == "Median Overall Survival") {
                        table.addCell(
                            Cells.createKey(
                                primaryEndPoint.value.toString() + " " + primaryEndPoint.unitOfMeasure + " (95% CI: " + (primaryEndPoint.confidenceInterval?.lowerLimit
                                    ?: "NA") + "-" + (primaryEndPoint.confidenceInterval?.upperLimit ?: "NA") + ")"
                            )
                        )
                    } else {
                        table.addCell(Cells.createKey("NA"))
                    }
                }
            }
        }
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createEmpty())
        return table
    }
}