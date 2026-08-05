package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionRuleMapper.Companion.VITAL_FUNCTION_NEGATIVE_MARGIN_OF_ERROR
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import java.time.LocalDate

class HasSufficientPulseOximetry internal constructor(
    private val minMedianPulseOximetry: Double, private val minimumDate: LocalDate, private val labels: EvaluationLabels.VitalFunction
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val relevant = VitalFunctionSelector.selectMedianPerDay(record, VitalFunctionCategory.SPO2, MAX_PULSE_OXIMETRY_TO_USE, minimumDate)
        val wrongUnit = VitalFunctionSelector.selectRecentVitalFunctionsWrongUnit(record, VitalFunctionCategory.SPO2)

        if (relevant.isEmpty()) {
            return EvaluationFactory.undetermined(
                if (wrongUnit.isEmpty()) {
                    labels.hasSufficientPulseOximetryUndeterminedNoData()
                } else {
                    labels.hasSufficientPulseOximetryUndeterminedWrongUnit(EXPECTED_UNIT)
                }
            )
        }

        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        val referenceWithMargin = minMedianPulseOximetry * VITAL_FUNCTION_NEGATIVE_MARGIN_OF_ERROR

        return when {
            median >= minMedianPulseOximetry -> {
                EvaluationFactory.recoverablePass(labels.hasSufficientPulseOximetryRecoverablePass(median, minMedianPulseOximetry))
            }

            (median >= referenceWithMargin) -> {
                EvaluationFactory.recoverableUndetermined(
                    labels.hasSufficientPulseOximetryRecoverableUndetermined(median, minMedianPulseOximetry)
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.hasSufficientPulseOximetryRecoverableFail(median, minMedianPulseOximetry))
            }
        }
    }

    companion object {
        private const val EXPECTED_UNIT: String = "percent"
        private const val MAX_PULSE_OXIMETRY_TO_USE = 5
    }
}