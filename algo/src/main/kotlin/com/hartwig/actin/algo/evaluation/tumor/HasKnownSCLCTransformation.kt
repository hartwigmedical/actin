package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.doid.DoidConstants.SMALL_CELL_LUNG_CANCER_DOIDS
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasKnownSCLCTransformation(private val doidModel: DoidModel, private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isNSCLC = DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID in DoidEvaluationFunctions.createFullExpandedDoidTree(
            doidModel,
            record.tumor.doids
        )
        val hasSmallCellDetails = TumorTypeEvaluationFunctions.hasTumorWithDetails(record.tumor, setOf("small cell", "mixed"))
        val hasNonSmallCellDetails =
            TumorTypeEvaluationFunctions.hasTumorWithDetails(record.tumor, setOf("non-small cell", "non small cell"))
        val hasMixedOrSmallCellDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel, record.tumor.doids, SMALL_CELL_LUNG_CANCER_DOIDS
        )
        val inactivatedGenes = listOf("TP53", "RB1").filter { MolecularRuleEvaluator.geneIsInactivatedForPatient(it, record, maxTestAge) }
        val amplifiedGenes = listOf("MYC").filter { MolecularRuleEvaluator.geneIsAmplifiedForPatient(it, record, maxTestAge) }

        return when {
            !isNSCLC -> {
                EvaluationFactory.fail("No lung cancer thus no SCLC transformation")
            }

            (hasSmallCellDetails && !hasNonSmallCellDetails) || hasMixedOrSmallCellDoid -> {
                EvaluationFactory.undetermined("NSCLC with mixed histology - undetermined if small cell transformation")
            }

            inactivatedGenes.isNotEmpty() || amplifiedGenes.isNotEmpty() -> {
                val eventMessage = sequenceOf(
                    inactivatedGenes.joinToString(", ") { "$it loss" },
                    amplifiedGenes.joinToString(", ") { "$it amp" }
                )
                    .filterNot(String::isEmpty)
                    .joinToString(", ")

                EvaluationFactory.undetermined("Undetermined small cell transformation ($eventMessage detected)")
            }

            else -> {
                EvaluationFactory.recoverableFail("No indication of small cell transformation in molecular or tumor doid data - assuming none")
            }
        }
    }
}