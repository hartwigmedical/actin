package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.util.ApplicationConfig

class HasSufficientBloodPressure internal constructor(
    private val category: BloodPressureCategory,
    private val minMedianBloodPressure: Double
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val relevant = VitalFunctionSelector.selectBloodPressures(record.clinical().vitalFunctions(), category)
        val categoryDisplay = category.display().lowercase(ApplicationConfig.LOCALE)
        if (relevant.isEmpty()) {
            return recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("No data found for $categoryDisplay")
                .build()
        }
        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        return when {
            median.compareTo(minMedianBloodPressure) >= 0 -> {
                EvaluationFactory.pass(
                    "Patient has median $categoryDisplay exceeding $minMedianBloodPressure",
                    "$categoryDisplay above limit"
                )
            }

            relevant.any { it.value().compareTo(minMedianBloodPressure) >= 0 } -> {
                EvaluationFactory.undetermined(
                    "Patient has median $categoryDisplay blood pressure below $minMedianBloodPressure "
                            + "but also at least one measure above $minMedianBloodPressure", "$categoryDisplay requirements"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient has median $categoryDisplay below $minMedianBloodPressure",
                    "$categoryDisplay below limit"
                )
            }
        }
    }
}