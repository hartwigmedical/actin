package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateVersusMinValueWithMargin
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue

class HasSufficientLabValue(
    private val minValue: Double,
    private val measurement: LabMeasurement,
    private val targetUnit: LabUnit,
    private val labels: EvaluationLabels.Laboratory
) : SingleLabValueEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit)
            ?: return EvaluationFactory.recoverableUndetermined(
                labels.hasSufficientLabValueRecoverableUndeterminedCouldNotConvert(labMeasurement.display(), targetUnit.display())
            )
        val labValueString = labValue(
            labMeasurement,
            convertedValue,
            targetUnit
        ) + (labels.hasSufficientLabValueConvertedFromSuffix(labValue.value, labValue.unit.display())
            .takeIf { convertedValue != labValue.value } ?: "")
        val refString = "$minValue ${targetUnit.display()}"

        return when (evaluateVersusMinValueWithMargin(convertedValue, labValue.comparator, minValue)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail(labels.hasSufficientLabValueRecoverableFail(labValueString, refString))
            }

            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.hasSufficientLabValueRecoverableUndeterminedWithinMargin(labValueString, refString)
                )
            }

            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.hasSufficientLabValueRecoverableUndeterminedCannotDetermine(
                        labMeasurement.display().replaceFirstChar { it.uppercase() }
                    )
                )
            }

            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass(labels.hasSufficientLabValuePass(labValueString, refString))
            }
        }
    }
}