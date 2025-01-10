package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.labReference
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue

class HasSufficientLabValueLLN(private val minLLNFactor: Double) : LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val labValueString = labValue(labMeasurement, labValue.value, labValue.unit)
        val referenceString = labReference(minLLNFactor, "LLN", labValue.refLimitLow)

        return when (LabEvaluation.evaluateVersusMinLLN(labValue, minLLNFactor)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail(
                    "$labValueString is below minimum of $referenceString", "$labValueString below min of $referenceString"
                )
            }

            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(
                    "$labValueString is below minimum of $referenceString", "$labValueString below min of $referenceString"
                )
            }

            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} could not be evaluated against minimum LLN",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined"
                )
            }

            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass(
                    "$labValueString above minimum of $referenceString", "$labValueString above min of $referenceString"
                )
            }
        }
    }
}