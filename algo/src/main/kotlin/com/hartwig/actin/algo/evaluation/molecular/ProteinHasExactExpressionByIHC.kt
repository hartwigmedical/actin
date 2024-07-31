package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class ProteinHasExactExpressionByIHC(private val protein: String, private val expressionLevel: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = PriorIHCTestFunctions.allIHCTestsForProtein(record.priorIHCTests, protein)
        for (ihcTest in ihcTests) {
            val scoreValue = ihcTest.scoreValue
            if (scoreValue != null) {
                // We assume IHC prior molecular tests always have integer score values.
                if (expressionLevel.toLong() == Math.round(scoreValue) && ihcTest.scoreValuePrefix.isNullOrEmpty()) {
                    return EvaluationFactory.pass(
                        "Protein $protein has exact expression level $expressionLevel by IHC",
                        "$protein has expression level of exactly $expressionLevel by IHC"
                    )
                }
            }
        }

        val hasPositiveOrNegativeResult = ihcTests.any {
            val scoreText = it.scoreText?.lowercase()
            scoreText == "positive" || scoreText == "negative"
        }

        return when {
            hasPositiveOrNegativeResult -> {
                EvaluationFactory.undetermined(
                    "Unknown if protein $protein expression level is exactly $expressionLevel by IHC",
                    "Exact expression level of $protein by IHC unknown"
                )
            }

            ihcTests.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "No test result found; protein $protein has not been tested by IHC",
                    "No $protein IHC test result"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Protein $protein does not have exact expression level $expressionLevel by IHC",
                    "Inadequate $protein expression level by IHC"
                )
            }
        }
    }
}