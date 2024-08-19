package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.labReference
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientLabValueULN(private val minULNFactor: Double) : LabEvaluationFunction {
    
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {

        val labValueString = labValue(labMeasurement, labValue.value)
        val referenceString = labReference(minULNFactor, "ULN", labValue.refLimitUp)

        return when (LabEvaluation.evaluateVersusMinULN(labValue, minULNFactor)) {
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
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} could not be evaluated versus maximum ULN",
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