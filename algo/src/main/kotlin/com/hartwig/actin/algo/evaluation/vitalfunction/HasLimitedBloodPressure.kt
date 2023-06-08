package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasLimitedBloodPressure internal constructor(
    private val category: BloodPressureCategory,
    private val maxMedianBloodPressure: Double
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
        return if (median.compareTo(maxMedianBloodPressure) <= 0) {
            EvaluationFactory.recoverablePass(
                "Patient has median $categoryDisplay below $maxMedianBloodPressure",
                "$categoryDisplay below limit"
            )
        } else if (relevant.any { it.value().compareTo(maxMedianBloodPressure) <= 0 }) {
            EvaluationFactory.recoverableUndetermined(
                "Patient has median $categoryDisplay blood pressure above $maxMedianBloodPressure "
                        + "but also at least one measure below $maxMedianBloodPressure", "$categoryDisplay requirements"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient has median $categoryDisplay exceeding $maxMedianBloodPressure",
                "$categoryDisplay above limit"
            )
        }
    }
}