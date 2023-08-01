package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasSufficientBloodPressure internal constructor(
    private val category: BloodPressureCategory,
    private val minMedianBloodPressure: Double
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
                    "Patient has median $categoryDisplay ($median) exceeding $minMedianBloodPressure",
                    "Median $categoryDisplay ($median) above limit of $minMedianBloodPressure"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient has median $categoryDisplay ($median) below $minMedianBloodPressure",
                    "Median $categoryDisplay ($median) below limit of $minMedianBloodPressure"
                )
            }
        }
    }
}