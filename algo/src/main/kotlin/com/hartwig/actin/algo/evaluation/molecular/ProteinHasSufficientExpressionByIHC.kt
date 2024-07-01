package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue

class ProteinHasSufficientExpressionByIHC(private val protein: String, private val minExpressionLevel: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = PriorMolecularTestFunctions.allIHCTestsForProtein(record.molecularHistory.allIHCTests(), protein)
        val evaluationsVersusReference = ihcTests.mapNotNull { ihcTest ->
            ihcTest.scoreValue?.let { scoreValue ->
                evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, minExpressionLevel.toDouble())
            }
        }.toSet()

        val hasPositiveOrNegativeResult = ihcTests.any {
            val scoreText = it.scoreText?.lowercase()
            scoreText == "positive" || scoreText == "negative"
        }

        return when {
            EvaluationResult.PASS in evaluationsVersusReference -> {
                EvaluationFactory.pass(
                    "Protein $protein has expression level of at least $minExpressionLevel by IHC",
                    "Adequate $protein expression level by IHC"
                )
            }

            EvaluationResult.UNDETERMINED in evaluationsVersusReference || hasPositiveOrNegativeResult -> {
                EvaluationFactory.undetermined(
                    "Unknown if protein $protein expression level is at least $minExpressionLevel by IHC",
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
                    "Protein $protein does not meet required expression level $minExpressionLevel by IHC",
                    "Inadequate $protein expression level by IHC"
                )
            }
        }
    }
}