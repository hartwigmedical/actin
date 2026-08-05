package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput

class HasCancerOfUnknownPrimary(
    private val doidModel: DoidModel, private val tumorType: TumorTypeInput, private val labels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasCancerOfUnknownPrimaryUndeterminedNoDoids())
        }
        val isCUP = TumorEvaluationFunctions.hasCancerOfUnknownPrimary(record.tumor.name)
        val hasTargetTumorType = DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, tumorDoids, tumorType.doid())
        val hasOrganSystemCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.ORGAN_SYSTEM_CANCER_DOID)

        return when {
            hasTargetTumorType && !hasOrganSystemCancer -> {
                if (isCUP) {
                    EvaluationFactory.pass(labels.hasCancerOfUnknownPrimaryPass())
                } else {
                    EvaluationFactory.warn(labels.hasCancerOfUnknownPrimaryWarn(record.tumor.name))
                }
            }

            DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.CANCER_DOID) -> {
                if (isCUP) {
                    EvaluationFactory.undetermined(
                        labels.hasCancerOfUnknownPrimaryUndeterminedCupButType(tumorType.display())
                    )
                } else {
                    EvaluationFactory.undetermined(labels.hasCancerOfUnknownPrimaryWarn(record.tumor.name))
                }
            }

            else -> EvaluationFactory.fail(labels.hasCancerOfUnknownPrimaryFail())
        }
    }
}