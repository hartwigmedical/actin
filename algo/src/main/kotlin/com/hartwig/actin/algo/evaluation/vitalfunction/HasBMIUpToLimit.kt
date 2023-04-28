package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.sort.BodyWeightDescendingDateComparator
import com.hartwig.actin.util.ApplicationConfig
import kotlin.math.sqrt

class HasBMIUpToLimit internal constructor(private val maximumBMI: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val latestWeight = record.clinical()
            .bodyWeights()
            .filter { it.unit().equals(EXPECTED_UNIT, ignoreCase = true) }
            .minWith(BodyWeightDescendingDateComparator())
            ?: return EvaluationFactory.undetermined("No body weights found in $EXPECTED_UNIT", "Body weight not provided")

        val minimumRequiredHeight = calculateHeightForBmiAndWeight(maximumBMI.toDouble(), latestWeight.value())
        return when {
            minimumRequiredHeight <= MIN_EXPECTED_HEIGHT_METRES -> {
                EvaluationFactory.pass(
                    String.format(
                        ApplicationConfig.LOCALE, "Patient weight %.1f kg will not exceed BMI limit of %d for height >= %.2f m",
                        latestWeight.value(), maximumBMI, minimumRequiredHeight
                    ), "BMI below limit"
                )
            }

            minimumRequiredHeight > MAX_EXPECTED_HEIGHT_METRES -> {
                EvaluationFactory.fail(
                    String.format(
                        ApplicationConfig.LOCALE,
                        "Patient weight %.1f kg will exceed BMI limit of %d for height < %.2f m", latestWeight.value(), maximumBMI,
                        minimumRequiredHeight
                    ), "BMI above limit"
                )
            }

            else -> {
                EvaluationFactory.warn(
                    String.format(
                        ApplicationConfig.LOCALE,
                        "Patient weight %.1f kg will exceed BMI limit of %d for height < %.2f m", latestWeight.value(), maximumBMI,
                        minimumRequiredHeight
                    ), "Potentially BMI above limit"
                )
            }
        }
    }

    companion object {
        private const val EXPECTED_UNIT: String = "kilogram"
        private const val MIN_EXPECTED_HEIGHT_METRES = 1.5
        private const val MAX_EXPECTED_HEIGHT_METRES = 2.0
        private fun calculateHeightForBmiAndWeight(bmi: Double, weight: Double): Double {
            return sqrt(weight / bmi)
        }
    }
}