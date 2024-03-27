package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientLabValueULN(private val minULNFactor: Double) : LabEvaluationFunction {
    
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val resultWithMargin = LabEvaluation.evaluateVersusMinULN(labValue, minULNFactor, true)
        val resultWithoutMargin = LabEvaluation.evaluateVersusMinULN(labValue, minULNFactor, false)

        val labValueString = "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${String.format("%.1f", labValue.value)}"
        val referenceString = "$minULNFactor*ULN ($minULNFactor*${labValue.refLimitUp})"

        return when {
            resultWithMargin == EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(
                    "$labValueString is below minimum of $referenceString", "$labValueString below min of $referenceString"
                )
            }
            resultWithoutMargin == EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableUndetermined(
                    "$labValueString is below minimum of $referenceString", "$labValueString below min of $referenceString"
                )
            }
            resultWithoutMargin == EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} could not be evaluated versus maximum ULN",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined"
                )
            }
            resultWithoutMargin == EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(
                    "$labValueString above minimum of $referenceString", "$labValueString above min of $referenceString"
                )
            }
            else -> {
                Evaluation(result = resultWithoutMargin, recoverable = true)
            }
        }
    }
}