package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.labReferenceWithLimit
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue

class HasLimitedLabValueULN(private val maxULNFactor: Double) : LabEvaluationFunction {
    
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val labValueString = labValue(labMeasurement, labValue.value, labValue.unit)
        val referenceString = labReferenceWithLimit(maxULNFactor, "ULN", labValue.refLimitUp, labValue.unit)

        return when (LabEvaluation.evaluateVersusMaxULN(labValue, maxULNFactor)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail("$labValueString exceeds max of $referenceString")
            }

            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined("$labValueString exceeds max of $referenceString")
            }

            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined("${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined")
            }

            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass("$labValueString below max of $referenceString")
            }
        }
    }
}