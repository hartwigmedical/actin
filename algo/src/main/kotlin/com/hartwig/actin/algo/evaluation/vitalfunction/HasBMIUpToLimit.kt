package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.EXPECTED_UNITS
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.util.ApplicationConfig
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.math.sqrt

class HasBMIUpToLimit(
    private val maximumBMI: Int, private val minimumDate: LocalDate, private val labels: EvaluationLabels.VitalFunction
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val allBodyWeights = record.bodyWeights
        val relevant = BodyWeightFunctions.selectMedianBodyWeightPerDay(record, minimumDate) ?: return EvaluationFactory.recoverableUndetermined(
            if (allBodyWeights.isNotEmpty() && allBodyWeights.none { weight -> EXPECTED_UNITS.any { it.equals(weight.unit, ignoreCase = true) } }) {
                labels.hasBmiUpToLimitUndeterminedWrongUnit(EXPECTED_UNITS.first())
            } else {
                labels.hasBmiUpToLimitUndeterminedNoData()
            }
        )
        val median = BodyWeightFunctions.determineMedianBodyWeight(relevant)
        val height = record.bodyHeights.maxOfOrNull { it.value }?.div(100)
        val bodyMassIndex = if (height != null) median / (height * height) else null
        val minimumRequiredHeight = calculateHeightForBmiAndWeight(maximumBMI.toDouble(), median)

        return when {
            bodyMassIndex != null && (bodyMassIndex <= maximumBMI) -> {
                EvaluationFactory.recoverablePass(labels.hasBmiUpToLimitRecoverablePass(bodyMassIndex.roundToInt(), maximumBMI))
            }

            bodyMassIndex != null && (bodyMassIndex > maximumBMI) -> {
                EvaluationFactory.recoverableFail(labels.hasBmiUpToLimitRecoverableFail(bodyMassIndex.roundToInt(), maximumBMI))
            }

            minimumRequiredHeight <= MIN_EXPECTED_HEIGHT_METRES -> {
                EvaluationFactory.recoverablePass(
                    labels.hasBmiUpToLimitPassHeightLowerBound(
                        String.format(ApplicationConfig.LOCALE, "%.1f", median),
                        maximumBMI,
                        String.format(ApplicationConfig.LOCALE, "%.2f", minimumRequiredHeight)
                    )
                )
            }

            minimumRequiredHeight > MAX_EXPECTED_HEIGHT_METRES -> {
                EvaluationFactory.recoverableFail(
                    labels.hasBmiUpToLimitExceedsLimitForHeight(
                        String.format(ApplicationConfig.LOCALE, "%.1f", median),
                        maximumBMI,
                        String.format(ApplicationConfig.LOCALE, "%.2f", minimumRequiredHeight)
                    )
                )
            }

            else -> {
                EvaluationFactory.warn(
                    labels.hasBmiUpToLimitExceedsLimitForHeight(
                        String.format(ApplicationConfig.LOCALE, "%.1f", median),
                        maximumBMI,
                        String.format(ApplicationConfig.LOCALE, "%.2f", minimumRequiredHeight)
                    )
                )
            }
        }
    }

    companion object {
        private const val MIN_EXPECTED_HEIGHT_METRES = 1.5
        private const val MAX_EXPECTED_HEIGHT_METRES = 2.0

        private fun calculateHeightForBmiAndWeight(bmi: Double, weight: Double): Double {
            return sqrt(weight / bmi)
        }
    }
}