package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator
import com.hartwig.actin.doid.DoidModel

class HasKnownSCLCTransformation(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val hasMixedOrSmallCellDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel, record.tumor.doids, SMALL_CELL_LUNG_CANCER_DOIDS
        )
        val molecularProfileEvaluation = hasSmallCellMolecularProfile(record)

        return when {
            hasMixedOrSmallCellDoid -> {
                EvaluationFactory.undetermined(
                    "Patient has tumor with (mixed) small cell histology - undetermined if small cell transformation",
                    "Tumor has (mixed) small cell histology - undetermined if small cell transformation",
                )
            }

            molecularProfileEvaluation.first -> {
                val inactivatedGenes = molecularProfileEvaluation.second
                val amplifiedGenes = molecularProfileEvaluation.third
                val inactivatedGenesMessage = if (inactivatedGenes.isNotEmpty()) {
                    inactivatedGenes.joinToString(", ", postfix = if (amplifiedGenes.isNotEmpty()) ", " else "") { "$it loss" }
                } else {
                    ""
                }
                val amplifiedGenesMessage = if (amplifiedGenes.isNotEmpty()) {
                    amplifiedGenes.joinToString(", ") { "$it amp" }
                } else {
                    ""
                }

                val message = "Undetermined small cell transformation ($inactivatedGenesMessage$amplifiedGenesMessage detected)"
                EvaluationFactory.undetermined(message, message)
            }

            else -> {
                val failMessage = "No indication of small cell transformation in molecular or tumor doid data - assuming none"
                EvaluationFactory.recoverableFail(failMessage, failMessage)
            }
        }
    }

    companion object {
        val SMALL_CELL_LUNG_CANCER_DOIDS = setOf(
            DoidConstants.LUNG_SMALL_CELL_CARCINOMA_DOID,
            DoidConstants.LUNG_OCCULT_SMALL_CELL_CARCINOMA_DOID,
            DoidConstants.LUNG_COMBINED_TYPE_SMALL_CELL_CARCINOMA_DOID,
            DoidConstants.LUNG_MIXED_SMALL_CELL_AND_SQUAMOUS_CELL_CARCINOMA_DOID,
            DoidConstants.LUNG_COMBINED_TYPE_SMALL_CELL_ADENOCARCINOMA_DOID
        )

        private fun hasSmallCellMolecularProfile(record: PatientRecord): Triple<Boolean, List<String>, List<String>> {
            val inactivatedGenes = listOf("TP53", "RB1").filter { MolecularRuleEvaluator.geneIsInactivatedForPatient(it, record) }
            val amplifiedGenes = listOf("MYC").filter { MolecularRuleEvaluator.geneIsAmplifiedForPatient(it, record) }
            return Triple((inactivatedGenes + amplifiedGenes).isNotEmpty(), inactivatedGenes, amplifiedGenes)
        }
    }
}