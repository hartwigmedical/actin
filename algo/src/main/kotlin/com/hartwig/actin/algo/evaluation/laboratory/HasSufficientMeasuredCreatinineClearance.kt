package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue

private const val MINUTES_PER_DAY = 1440
private const val MICROMOLES_PER_MILLIMOLE = 1000.0
private const val ML_PER_LITER = 1000.0

internal class HasSufficientMeasuredCreatinineClearance(
    private val minCreatinineClearance: Double,
    private val method: MeasuredCreatinineClearanceMethod
): MultiLabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurements: Map<LabMeasurement, LabValue>): Evaluation {
        val creatinine = labMeasurements[LabMeasurement.CREATININE]
            ?: return EvaluationFactory.recoverableUndetermined("No ${LabMeasurement.CREATININE.display()} value available")

        if (labMeasurements.values.any { it.comparator.isNotEmpty() && it.comparator != "=" }) {
            return EvaluationFactory.recoverableUndetermined(
                "Cannot calculate creatinine clearance: comparator present on input value(s) makes the result direction ambiguous"
            )
        }

        val creatinineClearance = when (method) {
            MeasuredCreatinineClearanceMethod.DAILY_TOTAL -> {
                val creatinine24U = labMeasurements[LabMeasurement.CREATININE_24U]
                    ?: return EvaluationFactory.recoverableUndetermined("No ${LabMeasurement.CREATININE_24U.display()} value available")
                creatinine24U.value * MICROMOLES_PER_MILLIMOLE * ML_PER_LITER / (creatinine.value * MINUTES_PER_DAY)
            }

            MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION -> {
                val creatinineUrine = labMeasurements[LabMeasurement.CREATININE_URINE]
                    ?: return EvaluationFactory.recoverableUndetermined("No ${LabMeasurement.CREATININE_URINE.display()} value available")
                val urineVolume = labMeasurements[LabMeasurement.URINE_VOLUME_24H]
                    ?: return EvaluationFactory.recoverableUndetermined("No ${LabMeasurement.URINE_VOLUME_24H.display()} value available")
                creatinineUrine.value * urineVolume.value * MICROMOLES_PER_MILLIMOLE / (creatinine.value * MINUTES_PER_DAY)
            }
        }

        val unit = LabMeasurement.CREATININE_CLEARANCE_24H.defaultUnit
        val labValueString = labValue(LabMeasurement.CREATININE_CLEARANCE_24H, creatinineClearance, unit)
        val refString = "$minCreatinineClearance ${unit.display()}"

        return when (evaluateVersusMinValue(creatinineClearance, null, minCreatinineClearance)) {
            EvaluationResult.PASS -> EvaluationFactory.recoverablePass("Calculated $labValueString exceeds min of $refString")
            EvaluationResult.FAIL -> EvaluationFactory.recoverableFail("Calculated $labValueString below min of $refString")
            else -> EvaluationFactory.recoverableUndetermined("Calculated creatinine clearance undetermined")
        }
    }
}
