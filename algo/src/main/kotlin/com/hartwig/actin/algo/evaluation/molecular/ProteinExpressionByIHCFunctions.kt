package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IHCTest

enum class IHCExpressionComparisonType {
    LIMITED,
    SUFFICIENT,
    EXACT
}

class ProteinExpressionByIHCFunctions(
    private val protein: String, private val referenceExpressionLevel: Int, private val comparisonType: IHCExpressionComparisonType
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = IHCTestFilter.allIHCTestsForProtein(record.ihcTests, protein)
        val evaluationsVersusReference = ihcTests.mapNotNull { ihcTest ->
            ihcTest.scoreValue?.let { scoreValue -> evaluateValue(ihcTest, scoreValue) }
        }.toSet()

        val hasPositiveOrNegativeResult = ihcTests.any {
            val scoreText = it.scoreText?.lowercase()
            scoreText == "positive" || scoreText == "negative"
        }

        val comparisonText = when (comparisonType) {
            IHCExpressionComparisonType.LIMITED -> "at most"
            IHCExpressionComparisonType.SUFFICIENT -> "at least"
            IHCExpressionComparisonType.EXACT -> "exactly"
        }

        return when {
            EvaluationResult.PASS in evaluationsVersusReference -> {
                EvaluationFactory.pass("$protein has expression of $comparisonText $referenceExpressionLevel by IHC")
            }

            EvaluationResult.UNDETERMINED in evaluationsVersusReference || hasPositiveOrNegativeResult -> {
                EvaluationFactory.undetermined("Undetermined if $protein expression is $comparisonText $referenceExpressionLevel by IHC")
            }

            ihcTests.isEmpty() -> {
                EvaluationFactory.undetermined("No $protein IHC test result", isMissingMolecularResultForEvaluation = true)
            }

            else -> {
                EvaluationFactory.fail("$protein expression not $comparisonText $referenceExpressionLevel by IHC")
            }
        }
    }

    private fun evaluateValue(ihcTest: IHCTest, scoreValue: Double): EvaluationResult {
        return when (comparisonType) {
            IHCExpressionComparisonType.SUFFICIENT -> {
                evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, referenceExpressionLevel.toDouble())
            }

            IHCExpressionComparisonType.EXACT -> {
                when (referenceExpressionLevel.toLong() == Math.round(scoreValue) && ihcTest.scoreValuePrefix.isNullOrEmpty()) {
                    true -> EvaluationResult.PASS
                    false -> EvaluationResult.FAIL
                }
            }

            IHCExpressionComparisonType.LIMITED -> {
                evaluateVersusMaxValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, referenceExpressionLevel.toDouble())
            }
        }
    }
}