package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.PriorIHCTest

enum class IhcExpressionComparisonType {
    LIMITED,
    SUFFICIENT,
    EXACT
}

class ProteinExpressionByIHCFunctions(
    private val protein: String, private val referenceExpressionLevel: Int, private val comparisonType: IhcExpressionComparisonType
): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = PriorIHCTestFunctions.allIHCTestsForProtein(record.priorIHCTests, protein)
        val evaluationsVersusReference = ihcTests.mapNotNull { ihcTest ->
            ihcTest.scoreValue?.let { scoreValue -> evaluateValue(ihcTest, scoreValue) }
        }.toSet()

        val hasPositiveOrNegativeResult = ihcTests.any {
            val scoreText = it.scoreText?.lowercase()
            scoreText == "positive" || scoreText == "negative"
        }

        val comparisonText = when (comparisonType) {
            IhcExpressionComparisonType.LIMITED -> "at most"
            IhcExpressionComparisonType.SUFFICIENT -> "at least"
            IhcExpressionComparisonType.EXACT -> "exactly"
        }

            return when {
            EvaluationResult.PASS in evaluationsVersusReference -> {
                EvaluationFactory.pass(
                    "Protein $protein has expression level of $comparisonText $referenceExpressionLevel by IHC",
                    "Adequate $protein expression level by IHC"
                )
            }

            EvaluationResult.UNDETERMINED in evaluationsVersusReference || hasPositiveOrNegativeResult -> {
                EvaluationFactory.undetermined(
                    "Unknown if protein $protein expression level is $comparisonText $referenceExpressionLevel by IHC",
                    "Exact expression level of $protein by IHC unknown"
                )
            }

            ihcTests.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "No test result found; protein $protein has not been tested by IHC", "No $protein IHC test result"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Protein $protein expression level by IHC not $comparisonText $referenceExpressionLevel",
                    "Inadequate $protein expression level by IHC"
                )
            }
        }
    }

    private fun evaluateValue(ihcTest: PriorIHCTest, scoreValue: Double): EvaluationResult {
        return when (comparisonType) {
            IhcExpressionComparisonType.SUFFICIENT -> {
                evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, referenceExpressionLevel.toDouble())
            }
            IhcExpressionComparisonType.EXACT -> {
                when (referenceExpressionLevel.toLong() == Math.round(scoreValue) && ihcTest.scoreValuePrefix.isNullOrEmpty()) {
                    true -> EvaluationResult.PASS
                    false -> EvaluationResult.FAIL
                }
            }
            IhcExpressionComparisonType.LIMITED -> {
                evaluateVersusMaxValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, referenceExpressionLevel.toDouble())
            }
        }
    }
}