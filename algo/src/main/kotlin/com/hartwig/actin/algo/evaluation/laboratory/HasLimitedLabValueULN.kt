package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.labReference
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue

class HasLimitedLabValueULN(private val maxULNFactor: Double) : LabEvaluationFunction {
    
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {

        val labValueString = labValue(labMeasurement, labValue.value)
        val referenceString = labReference(maxULNFactor, "ULN", labValue.refLimitUp)

        return when (LabEvaluation.evaluateVersusMaxULN(labValue, maxULNFactor)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail(
                    "$labValueString exceeds maximum of $referenceString", "$labValueString exceeds max of $referenceString"
                )
            }

            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(
                    "$labValueString exceeds maximum of $referenceString", "$labValueString exceeds max of $referenceString"
                )
            }

            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} could not be evaluated against maximum ULN",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined"
                )
            }

            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass(
                    "$labValueString below maximum of $referenceString", "$labValueString below max of $referenceString"
                )
            }
        }
    }
}