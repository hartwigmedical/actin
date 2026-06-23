package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails

class HasSpecificMetastasesOnly(
    private val targetMetastases: List<(TumorDetails) -> Boolean?>,
    private val targetSuspectedMetastases: List<(TumorDetails) -> Boolean?>,
    private val typeOfMetastases: String
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
                        suspectedOtherMetastasesValues.none { it == true } && otherSuspectedLesions.isNullOrEmpty() &&
                        !hasSuspectedMetastasesOutsideTargetMetastases

            return when {
                hasTargetMetastases && hasNoMetastasesOutsideTargetMetastases -> {
                    EvaluationFactory.pass("Has only $typeOfMetastases metastases")
                }

                hasNoTargetMetastases || hasMetastasesOutsideTargetMetastases -> {
                    EvaluationFactory.fail("Does not have only $typeOfMetastases metastases")
                }

                hasSuspectedTargetMetastases || hasSuspectedMetastasesOutsideTargetMetastases -> {
                    EvaluationFactory.undetermined("Undetermined if patient has only $typeOfMetastases metastases (suspected lesions presence and/or missing lesion data)")
                }

                else -> {
                    EvaluationFactory.undetermined("Undetermined if patient has only $typeOfMetastases metastases (missing lesion data)")
                }
            }
        }
    }
}