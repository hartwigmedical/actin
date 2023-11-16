package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasSufficientBloodPressure internal constructor(
    private val category: BloodPressureCategory,
    private val minMedianBloodPressure: Int
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val relevant = VitalFunctionSelector.selectBloodPressures(record.clinical().vitalFunctions(), category)
        val categoryDisplay = category.display().lowercase()
        if (relevant.isEmpty()) {
            return recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("No data found for $categoryDisplay")
                .build()
        }
        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        return when {
            median.compareTo(minMedianBloodPressure) >= 0 -> {
                EvaluationFactory.recoverablePass(
                    "Patient has median $categoryDisplay (${median.toInt()} mmHg) exceeding $minMedianBloodPressure mmHg",
                    "Median $categoryDisplay (${median.toInt()} mmHg) above limit of $minMedianBloodPressure mmHg"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient has median $categoryDisplay (${median.toInt()} mmHg) below $minMedianBloodPressure mmHg",
                    "Median $categoryDisplay (${median.toInt()} mmHg) below limit of $minMedianBloodPressure mmHg"
                )
            }
        }
    }
}