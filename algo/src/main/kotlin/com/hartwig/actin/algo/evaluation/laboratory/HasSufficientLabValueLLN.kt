package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientLabValueLLN internal constructor(private val minLLNFactor: Double) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val result = LabEvaluation.evaluateVersusMinLLN(labValue, minLLNFactor)
        val labValueString = "${labMeasurement.display()} ${String.format("%.1f", labValue.value())}"
        val referenceString = "$minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow()})"

        return when (result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(
                    "$labValueString is below minimum of $referenceString", "$labValueString below min of $referenceString"
                )
            }
            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display()} could not be evaluated against minimum LLN", "${labMeasurement.display()} undetermined"
                )
            }
            EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(
                    "$labValueString above minimum of $referenceString", "$labValueString above min of $referenceString"
                )
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }
}