package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasNonSquamousNsclc(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasNonSquamousNsclcUndeterminedTumorTypeMissing())
        }

        val isSquamousNsclc = DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel,
            tumorDoids,
            setOf(DoidConstants.LUNG_SQUAMOUS_CELL_CARCINOMA_DOID, DoidConstants.LUNG_ADENOSQUAMOUS_CARCINOMA_DOID)
        )
        val isNonSquamousNsclc = DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel,
            tumorDoids,
            DoidConstants.LUNG_NON_SQUAMOUS_NSCLC_DOIDS
        )
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val isExactLungCarcinoma = DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.LUNG_CARCINOMA_DOID)
        val isExactLungCancer = DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.LUNG_CANCER_DOID)

        val ihcTestEvaluation = IhcTestEvaluation.create(item = "SCC transformation", ihcTests = record.ihcTests)

        return when {
            isSquamousNsclc -> EvaluationFactory.fail(labels.hasNonSquamousNsclcFail())

            isNonSquamousNsclc && ihcTestEvaluation.hasCertainBroadPositiveResultsForItem() -> {
                EvaluationFactory.warn(labels.hasNonSquamousNsclcWarnPositiveScc())
            }

            isNonSquamousNsclc && ihcTestEvaluation.hasPossiblePositiveResultsForItem() -> {
                EvaluationFactory.warn(labels.hasNonSquamousNsclcWarnPossiblePositiveScc())
            }

            isNonSquamousNsclc -> EvaluationFactory.pass(labels.hasNonSquamousNsclcPass())

            isNsclc || isExactLungCarcinoma || isExactLungCancer -> EvaluationFactory.undetermined(
                labels.hasNonSquamousNsclcUndeterminedType()
            )

            else -> EvaluationFactory.fail(labels.hasNonSquamousNsclcFail())
        }
    }
}
