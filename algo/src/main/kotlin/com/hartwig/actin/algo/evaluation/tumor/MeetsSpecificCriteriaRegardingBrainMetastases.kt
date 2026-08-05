package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MeetsSpecificCriteriaRegardingBrainMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownBrainLesions = hasBrainLesions == null

            // We assume that if a patient has active brain metastases, hasBrainMetastases is allowed to be (theoretically) null/false
            return when {
                hasActiveBrainLesions == true -> {
                    EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingBrainMetastasesUndetermined())
                }

                hasBrainLesions == true -> {
                    EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingBrainMetastasesUndetermined())
                }

                hasSuspectedBrainLesions == true -> {
                    EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingBrainMetastasesUndeterminedSuspected())
                }

                unknownBrainLesions && hasCnsLesions == true -> {
                    EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingBrainMetastasesUndetermined())
                }

                unknownBrainLesions && hasSuspectedCnsLesions == true -> {
                    EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingBrainMetastasesUndeterminedSuspected())
                }

                unknownBrainLesions -> {
                    EvaluationFactory.undetermined(labels.meetsSpecificCriteriaRegardingBrainMetastasesUndeterminedMissing())
                }

                else -> {
                    EvaluationFactory.fail(labels.meetsSpecificCriteriaRegardingBrainMetastasesFail())
                }
            }
        }
    }
}