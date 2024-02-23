package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.algo.datamodel.StandardOfCareMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class EligibleApprovedTreatmentGenerator(
    private val clinical: ClinicalRecord, private val molecular: MolecularRecord, private val treatments: List<StandardOfCareMatch>?,
    private val width: Float, private val mode: String
) : TableGenerator {

    override fun title(): String {
        return "Standard of care options considered potentially eligible"
    }

    override fun contents(): Table {
        when (mode) {
            "Other" -> {
                val table = Tables.createSingleColWithWidth(width)
                table.addHeaderCell(Cells.createHeader("Treatment"))
                val isCUP = TumorDetailsInterpreter.isCUP(clinical.tumor)
                val hasConfidentPrediction = TumorOriginInterpreter.hasConfidentPrediction(molecular.characteristics.predictedTumorOrigin)
                if (isCUP && hasConfidentPrediction) {
                    table.addCell(Cells.createContent("Potential SOC for " + molecular.characteristics.predictedTumorOrigin!!.cancerType()))
                } else {
                    table.addCell(Cells.createContent("Not yet determined"))
                }
                return makeWrapping(table)
            }

            "CRC" -> {
                val table = Tables.createFixedWidthCols(1f, 1f, 1f).setWidth(width)
                table.addHeaderCell(Cells.createHeader("Treatment"))
                table.addHeaderCell(Cells.createHeader("Literature based"))
                table.addHeaderCell(Cells.createHeader("Personalized PFS"))
                treatments?.forEach { treatment: StandardOfCareMatch ->
                    table.addCell(Cells.createContentBold(treatment.treatmentCandidate.treatment.name))
                    val subtable = Tables.createFixedWidthCols(50f, 150f).setWidth(200f)
                    if (treatment.annotations.isNullOrEmpty()) {
                        subtable.addCell(Cells.createValue(" "))
                        subtable.addCell(Cells.createValue("No literature efficacy evidence available yet"))
                    } else {
                        for (annotation in treatment.annotations!!) {
                            val paper = annotation.trialReferences.iterator().next() // for now assume we only have 1 paper per trial
                            for (patientPopulation in paper.patientPopulations) {
                                if (generateOptions(
                                        patientPopulation.therapy!!.synonyms ?: patientPopulation.therapy!!.therapyName
                                    ).any { therapy -> therapy == treatment.treatmentCandidate.treatment.name }
                                ) {
                                    val analysisGroup = patientPopulation.analysisGroups.iterator()
                                        .next() // assume only 1 analysis group per patient population
                                    subtable.addCell(Cells.createValue("PFS: "))
                                    for (primaryEndPoint in analysisGroup.primaryEndPoints!!) {
                                        if (primaryEndPoint.name == "Median Progression-Free Survival") {
                                            subtable.addCell(
                                                Cells.createKey(
                                                    primaryEndPoint.value.toString() + " " + primaryEndPoint.unitOfMeasure + " (95% CI: " + (primaryEndPoint.confidenceInterval?.lowerLimit
                                                        ?: "NA") + "-" + (primaryEndPoint.confidenceInterval?.upperLimit ?: "NA") + ")"
                                                )
                                            )
                                        } else {
                                            subtable.addCell(Cells.createKey("NA"))
                                        }
                                    }

                                    subtable.addCell(Cells.createValue("OS: "))
                                    for (primaryEndPoint in analysisGroup.primaryEndPoints!!) {
                                        if (primaryEndPoint.name == "Median Overall Survival") {
                                            subtable.addCell(
                                                Cells.createKey(
                                                    primaryEndPoint.value.toString() + " " + primaryEndPoint.unitOfMeasure.display() + " (95% CI: " + (primaryEndPoint.confidenceInterval?.lowerLimit
                                                        ?: "NA") + "-" + (primaryEndPoint.confidenceInterval?.upperLimit ?: "NA") + ")"
                                                )
                                            )
                                        } else {
                                            subtable.addCell(Cells.createKey("NA"))
                                        }
                                    }
                                    subtable.addCell(Cells.createEmpty())
                                    subtable.addCell(Cells.createValue("(${annotation.acronym})"))
                                    subtable.addCell(Cells.createValue(" "))
                                    subtable.addCell(Cells.createValue(" "))
                                }
                            }
                        }
                    }
                    table.addCell(Cells.createContent(subtable))
                    table.addCell(Cells.createContent("Not evaluated yet"))
                }
                return makeWrapping(table)
            }

            else -> throw IllegalStateException("Unknown mode: $mode")
        }
    }

    private fun generateOptions(therapy: String): List<String> {
        return therapy.split("|").flatMap { item ->
            if (item.contains(" + ")) {
                createPermutations(item)
            } else {
                listOf(item)
            }
        }
    }

    private fun createPermutations(treatment: String): List<String> {
        val drugs = treatment.split(" + ")
        return listOf(drugs.joinToString("+"), drugs.reversed().joinToString("+"))
    }
}