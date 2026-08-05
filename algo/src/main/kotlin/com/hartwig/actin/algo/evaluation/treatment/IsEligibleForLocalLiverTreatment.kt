package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class IsEligibleForLocalLiverTreatment(
    private val doidModel: DoidModel,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverLesions = record.tumor.hasLiverLesions
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedParentsDoidTree(doidModel, record.tumor.doids)
        val hasLiverCancer = DoidConstants.LIVER_CANCER_DOID in expandedDoidSet

        return when {
            hasLiverCancer && hasLiverLesions != true -> {
                EvaluationFactory.undetermined(labels.isEligibleForLocalLiverTreatmentUndeterminedLiverCancer())
            }

            hasLiverLesions == false -> {
                EvaluationFactory.fail(labels.isEligibleForLocalLiverTreatmentFail())
            }

            hasLiverLesions == true -> {
                EvaluationFactory.undetermined(labels.isEligibleForLocalLiverTreatmentUndetermined())
            }

            else -> {
                EvaluationFactory.undetermined(labels.isEligibleForLocalLiverTreatmentUndeterminedLesionsUnknown())
            }
        }
    }
}