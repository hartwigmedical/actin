package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue

private const val MINUTES_PER_DAY = 1440
private const val MICROMOLES_PER_MILLIMOLE = 1000.0
private const val ML_PER_LITER = 1000.0
private const val MEASURED_CREATININE_CLEARANCE = "Measured creatinine clearance"
private const val MEASURED_CREATININE_CLEARANCE_UNDETERMINED_MESSAGE = "$MEASURED_CREATININE_CLEARANCE undetermined"

internal class HasSufficientMeasuredCreatinineClearance(
    private val minCreatinineClearance: Double,
    private val method: MeasuredCreatinineClearanceMethod
) : MultiLabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurements: Map<LabMeasurement, LabValue>): Evaluation {
        val creatinine = labMeasurements[LabMeasurement.CREATININE]
            ?: return EvaluationFactory.recoverableUndetermined("$MEASURED_CREATININE_CLEARANCE_UNDETERMINED_MESSAGE: No ${LabMeasurement.CREATININE.display()} value available")

        if (creatinine.value == 0.0) {
            return EvaluationFactory.recoverableUndetermined(
                "$MEASURED_CREATININE_CLEARANCE_UNDETERMINED_MESSAGE: ${LabMeasurement.CREATININE.display()} value is zero"
            )
        }

        if (labMeasurements.values.any { it.comparator.isNotEmpty() && it.comparator != "=" }) {
            return EvaluationFactory.recoverableUndetermined(
                "$MEASURED_CREATININE_CLEARANCE_UNDETERMINED_MESSAGE: comparator present on input value(s) makes the result direction ambiguous"
            )
        }

        val creatinineClearance = when (method) {
            MeasuredCreatinineClearanceMethod.DAILY_TOTAL -> {
                val creatinine24U = labMeasurements[LabMeasurement.CREATININE_24U]
                    ?: return EvaluationFactory.recoverableUndetermined("$MEASURED_CREATININE_CLEARANCE_UNDETERMINED_MESSAGE: No ${LabMeasurement.CREATININE_24U.display()} value available")
                creatinine24U.value * MICROMOLES_PER_MILLIMOLE * ML_PER_LITER / (creatinine.value * MINUTES_PER_DAY)
            }

            MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION -> {
                val creatinineUrine = labMeasurements[LabMeasurement.CREATININE_URINE]
                    ?: return EvaluationFactory.recoverableUndetermined("$MEASURED_CREATININE_CLEARANCE_UNDETERMINED_MESSAGE: No ${LabMeasurement.CREATININE_URINE.display()} value available")
                val urineVolume = labMeasurements[LabMeasurement.URINE_VOLUME_24H]
                    ?: return EvaluationFactory.recoverableUndetermined("$MEASURED_CREATININE_CLEARANCE_UNDETERMINED_MESSAGE: No ${LabMeasurement.URINE_VOLUME_24H.display()} value available")
                creatinineUrine.value * urineVolume.value * MICROMOLES_PER_MILLIMOLE / (creatinine.value * MINUTES_PER_DAY)
            }
        }

        val unit = LabMeasurement.CREATININE_CLEARANCE_24H.defaultUnit
        val refString = "$minCreatinineClearance ${unit.display()}"

        return when (evaluateVersusMinValue(creatinineClearance, null, minCreatinineClearance)) {
            EvaluationResult.PASS -> EvaluationFactory.recoverablePass("$MEASURED_CREATININE_CLEARANCE exceeds min of $refString")
            EvaluationResult.FAIL -> EvaluationFactory.recoverableFail("$MEASURED_CREATININE_CLEARANCE below min of $refString")
            else -> EvaluationFactory.recoverableUndetermined(MEASURED_CREATININE_CLEARANCE_UNDETERMINED_MESSAGE)
        }
    }
}
