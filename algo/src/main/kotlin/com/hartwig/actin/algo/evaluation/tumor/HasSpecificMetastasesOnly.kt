package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails

class HasSpecificMetastasesOnly(
    private val targetMetastases: List<(TumorDetails) -> Boolean?>,
    private val targetSuspectedMetastases: List<(TumorDetails) -> Boolean?>,
    private val typeOfMetastases: String,
    private val labels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val targetMetastasesValues = targetMetastases.map { it(this) }
            val suspectedTargetMetastasesValues = targetSuspectedMetastases.map { it(this) }

            val hasTargetMetastases = targetMetastasesValues.any { it == true }
            val hasSuspectedTargetMetastases = suspectedTargetMetastasesValues.any { it == true }
            val hasNoTargetMetastases = targetMetastasesValues.all { it == false } && !hasSuspectedTargetMetastases

            val otherMetastasesValues = targetMetastasesValues.fold(confirmedCategoricalLesionList()) { acc, v -> acc - v }
            val suspectedOtherMetastasesValues =
                suspectedTargetMetastasesValues.fold(suspectedCategoricalLesionList()) { acc, v -> acc - v }

            val hasMetastasesOutsideTargetMetastases = otherMetastasesValues.any { it == true } || otherLesions?.isNotEmpty() == true
            val hasSuspectedMetastasesOutsideTargetMetastases =
                suspectedOtherMetastasesValues.any { it == true } || otherSuspectedLesions?.isNotEmpty() == true
            val hasNoMetastasesOutsideTargetMetastases =
                otherMetastasesValues.all { it == false } && otherLesions?.isEmpty() == true &&
                        suspectedOtherMetastasesValues.none { it == true } && otherSuspectedLesions.isNullOrEmpty()

            return when {
                hasTargetMetastases && hasNoMetastasesOutsideTargetMetastases -> {
                    EvaluationFactory.pass(labels.hasSpecificMetastasesOnlyPass(typeOfMetastases))
                }

                hasNoTargetMetastases || hasMetastasesOutsideTargetMetastases -> {
                    EvaluationFactory.fail(labels.hasSpecificMetastasesOnlyFail(typeOfMetastases))
                }

                hasSuspectedTargetMetastases || hasSuspectedMetastasesOutsideTargetMetastases -> {
                    EvaluationFactory.undetermined(labels.hasSpecificMetastasesOnlyUndeterminedSuspected(typeOfMetastases))
                }

                else -> {
                    EvaluationFactory.undetermined(labels.hasSpecificMetastasesOnlyUndeterminedMissing(typeOfMetastases))
                }
            }
        }
    }
}