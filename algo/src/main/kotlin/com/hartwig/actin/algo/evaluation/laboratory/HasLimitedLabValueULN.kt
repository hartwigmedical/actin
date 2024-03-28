package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasLimitedLabValueULN(private val maxULNFactor: Double) : LabEvaluationFunction {
    
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val result = LabEvaluation.evaluateVersusMaxULN(labValue, maxULNFactor)

        val labValueString = "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${String.format("%.1f", labValue.value)}"
        val referenceString = "$maxULNFactor*ULN ($maxULNFactor*${labValue.refLimitUp})"

        return when {
            result == EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(
                    "$labValueString exceeds maximum of $referenceString", "$labValueString exceeds max of $referenceString"
                )
            }
            result == EvaluationResult.WARN -> {
                EvaluationFactory.recoverableUndetermined(
                    "$labValueString exceeds maximum of $referenceString", "$labValueString exceeds max of $referenceString"
                )
            }
            result == EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} could not be evaluated against maximum ULN",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined"
                )
            }
            result == EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(
                    "$labValueString below maximum of $referenceString", "$labValueString below max of $referenceString"
                )
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }
}