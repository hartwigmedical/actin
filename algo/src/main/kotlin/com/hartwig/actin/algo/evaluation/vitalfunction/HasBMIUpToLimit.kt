package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.util.ApplicationConfig
import java.time.LocalDate
import kotlin.math.sqrt

class HasBMIUpToLimit(private val maximumBMI: Int, private val minimumDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val allBodyWeights = record.bodyWeights
        val relevant = BodyWeightFunctions.selectMedianBodyWeightPerDay(record, minimumDate) ?: return EvaluationFactory.undetermined(
            if (allBodyWeights.isNotEmpty() && allBodyWeights.none { it.unit.equals(EXPECTED_UNIT, ignoreCase = true) }) {
                "Body weights not measured in $EXPECTED_UNIT"
            } else {
                "No (recent) body weights found"
            }
        )
        val median = BodyWeightFunctions.determineMedianBodyWeight(relevant)

        val minimumRequiredHeight = calculateHeightForBmiAndWeight(maximumBMI.toDouble(), median)

        return when {
            minimumRequiredHeight <= MIN_EXPECTED_HEIGHT_METRES -> {
                EvaluationFactory.pass(
                    String.format(
                        ApplicationConfig.LOCALE, "Median weight %.1f kg will not exceed BMI limit of %d for height >= %.2f m",
                        median, maximumBMI, minimumRequiredHeight
                    ), "BMI below limit"
                )
            }

            minimumRequiredHeight > MAX_EXPECTED_HEIGHT_METRES -> {
                EvaluationFactory.fail(
                    String.format(
                        ApplicationConfig.LOCALE,
                        "Median weight %.1f kg will exceed BMI limit of %d for height < %.2f m", median, maximumBMI,
                        minimumRequiredHeight
                    ), "BMI above limit"
                )
            }

            else -> {
                EvaluationFactory.warn(
                    String.format(
                        ApplicationConfig.LOCALE,
                        "Median weight %.1f kg will exceed BMI limit of %d for height < %.2f m", median, maximumBMI,
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