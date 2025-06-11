package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasKnownSclcTransformation(private val doidModel: DoidModel, private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isLungCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID)
        val isOfUncertainLungCancerType = DoidEvaluationFunctions.isOfExactDoid(
            record.tumor.doids,
            DoidConstants.LUNG_CANCER_DOID
        ) || DoidEvaluationFunctions.isOfExactDoid(record.tumor.doids, DoidConstants.LUNG_CARCINOMA_DOID)
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val isSclc =
            DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, record.tumor.doids, DoidConstants.SMALL_CELL_LUNG_CANCER_DOIDS)
        val hasSmallCellComponent =
            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(doidModel, record.tumor.doids, record.tumor.name)

        val inactivatedGenes = listOf("TP53", "RB1").filter { MolecularRuleEvaluator.geneIsInactivatedForPatient(it, record, maxTestAge) }

        return when {
            isNsclc && (isSclc || hasSmallCellComponent) -> {
                EvaluationFactory.undetermined("NSCLC with small cell component detected - undetermined if considered small cell transformation")
            }

            isNsclc && inactivatedGenes.isNotEmpty() -> {
                val genes = inactivatedGenes.joinToString(" and ")
                EvaluationFactory.undetermined("Undetermined if small cell transformation may have occurred ($genes inactivation detected)")
            }

            isOfUncertainLungCancerType -> {
                EvaluationFactory.undetermined("Undetermined if NSCLC and hence if there may be small cell transformation")
            }

            !isLungCancer -> {
                EvaluationFactory.fail("No lung cancer thus no small cell transformation")
            }

            else -> {
                EvaluationFactory.recoverableFail("No indication of small cell transformation in molecular or tumor type data")
            }
        }
    }
}