package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import kotlin.math.roundToInt

class HasLimitedBloodPressure internal constructor(
    private val category: BloodPressureCategory,
    private val maxMedianBloodPressure: Int
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
                "Patient has median $categoryDisplay (${median.roundToInt()} mmHg) below $maxMedianBloodPressure mmHg",
                "Median $categoryDisplay (${median.roundToInt()} mmHg) below limit of $maxMedianBloodPressure mmHg"
            )

        } else {
            EvaluationFactory.recoverableFail(
                "Patient has median $categoryDisplay (${median.roundToInt()} mmHg) exceeding $maxMedianBloodPressure mmHg",
                "Median $categoryDisplay (${median.roundToInt()} mmHg) above limit of $maxMedianBloodPressure mmHg"
            )
        }
    }
}