package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasKnownSclcTransformation(
    private val doidModel: DoidModel,
    private val molecularLabels: EvaluationLabels.Molecular,
    private val tumorLabels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isLungCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID)
        val isOfUncertainLungCancerType = listOf(
            DoidConstants.LUNG_CANCER_DOID,
            DoidConstants.LUNG_CARCINOMA_DOID
        ).any { DoidEvaluationFunctions.isOfExactDoid(record.tumor.doids, it) }
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val isSclc =
            DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, record.tumor.doids, DoidConstants.SMALL_CELL_LUNG_CANCER_DOIDS)
        val hasSmallCellComponent =
            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(doidModel, record.tumor.doids, record.tumor.name)

        val ihcTestEvaluations =
            listOf("SCLC transformation", "small cell transformation").map { IhcTestEvaluation.create(it, record.ihcTests) }

        val indicativeGenes = setOf("TP53", "RB1")
        val allIndicativeGenesInactivated =
            indicativeGenes.all { MolecularRuleEvaluator.geneIsInactivatedForPatient(it, record, molecularLabels) }

        return when {
            isNsclc && ihcTestEvaluations.any(IhcTestEvaluation::hasCertainBroadPositiveResultsForItem) -> {
                EvaluationFactory.pass(tumorLabels.hasKnownSclcTransformationPass(), inclusionEvents = setOf("small cell transformation"))
            }

            isNsclc && ihcTestEvaluations.any(IhcTestEvaluation::hasPossiblePositiveResultsForItem) -> {
                EvaluationFactory.warn(tumorLabels.hasKnownSclcTransformationWarn())
            }

            isNsclc && (isSclc || hasSmallCellComponent) -> {
                EvaluationFactory.undetermined(tumorLabels.hasKnownSclcTransformationUndeterminedSmallCellComponent())
            }

            isNsclc && allIndicativeGenesInactivated -> {
                EvaluationFactory.undetermined(
                    tumorLabels.hasKnownSclcTransformationUndeterminedInactivation(Format.concat(indicativeGenes))
                )
            }

            isOfUncertainLungCancerType -> {
                EvaluationFactory.undetermined(tumorLabels.hasKnownSclcTransformationUndeterminedUncertainType())
            }

            !isLungCancer -> EvaluationFactory.fail(tumorLabels.hasKnownSclcTransformationFail())

            else -> EvaluationFactory.recoverableFail(tumorLabels.hasKnownSclcTransformationRecoverableFail())
        }
    }
}