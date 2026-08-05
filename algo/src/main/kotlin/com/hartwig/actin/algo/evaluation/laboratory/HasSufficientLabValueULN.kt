package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.labReferenceWithLimit
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue

class HasSufficientLabValueULN(private val minULNFactor: Double, private val labels: EvaluationLabels.Laboratory) :
    SingleLabValueEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val labValueString = labValue(labMeasurement, labValue.value, labValue.unit)
        val referenceString = labReferenceWithLimit(minULNFactor, "ULN", labValue.refLimitUp, labValue.unit)

        return when (LabEvaluation.evaluateVersusMinULN(labValue, minULNFactor)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail(labels.hasSufficientLabValueUlnExceedsMin(labValueString, referenceString))
            }

            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(labels.hasSufficientLabValueUlnExceedsMin(labValueString, referenceString))
            }

            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.hasSufficientLabValueUlnRecoverableUndeterminedCannotDetermine(
                        labMeasurement.display().replaceFirstChar { it.uppercase() }
                    )
                )
            }

            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass(labels.hasSufficientLabValueUlnPass(labValueString, referenceString))
            }
        }
    }
}