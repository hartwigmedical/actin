package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants.SMALL_CELL_LUNG_CANCER_DOIDS
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator
import com.hartwig.actin.doid.DoidModel

class HasKnownSCLCTransformation(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val hasMixedOrSmallCellDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel, record.tumor.doids, SMALL_CELL_LUNG_CANCER_DOIDS
        )
        val inactivatedGenes = listOf("TP53", "RB1").filter { MolecularRuleEvaluator.geneIsInactivatedForPatient(it, record) }
        val amplifiedGenes = listOf("MYC").filter { MolecularRuleEvaluator.geneIsAmplifiedForPatient(it, record) }

        return when {
            hasMixedOrSmallCellDoid -> {
                EvaluationFactory.undetermined(
                    "Patient has tumor with (mixed) small cell histology - undetermined if small cell transformation",
                    "Tumor has (mixed) small cell histology - undetermined if small cell transformation",
                )
            }

            inactivatedGenes.isNotEmpty() || amplifiedGenes.isNotEmpty() -> {
                val eventMessage = sequenceOf(
                    inactivatedGenes.joinToString(", ") { "$it loss" },
                    amplifiedGenes.joinToString(", ") { "$it amp" }
                )
                    .filterNot(String::isEmpty)
                    .joinToString(", ")

                val message = "Undetermined small cell transformation ($eventMessage detected)"
                EvaluationFactory.undetermined(message, message)
            }

            else -> {
                val failMessage = "No indication of small cell transformation in molecular or tumor doid data - assuming none"
                EvaluationFactory.recoverableFail(failMessage, failMessage)
            }
        }
    }
}