package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class ProteinHasExactExpressionByIHC(private val protein: String, private val expressionLevel: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = PriorMolecularTestFunctions.allIHCTestsForProtein(record.clinical().priorMolecularTests(), protein)
        var hasPositiveOrNegativeResult = false
        for (ihcTest in ihcTests) {
            val scoreValue = ihcTest.scoreValue()
            if (scoreValue != null) {
                val scoreValuePrefix = ihcTest.scoreValuePrefix()
                // We assume IHC prior molecular tests always have integer score values.
                if (expressionLevel.toLong() == Math.round(scoreValue) && scoreValuePrefix.isNullOrEmpty()) {
                    return unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Protein $protein has exact expression level $expressionLevel by IHC")
                        .addPassGeneralMessages("$protein has expression level of exactly $expressionLevel by IHC")
                        .build()
                }
            }
            val scoreText = ihcTest.scoreText()
            if (scoreText != null && (scoreText.equals("positive", ignoreCase = true) || scoreText.equals("negative", ignoreCase = true))) {
                hasPositiveOrNegativeResult = true
            }
        }
        if (hasPositiveOrNegativeResult) {
            return unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    "Unknown if protein $protein expression level is exactly $expressionLevel by IHC"
                )
                .addUndeterminedGeneralMessages("Exact expression level of $protein by IHC unknown")
                .build()
        } else if (ihcTests.isEmpty()) {
            return unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("No test result found; protein $protein has not been tested by IHC")
                .addUndeterminedGeneralMessages("No $protein IHC test result")
                .build()
        }
        return unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("Protein $protein does not have exact expression level $expressionLevel by IHC")
            .addFailGeneralMessages("Inadequate $protein expression level by IHC")
            .build()
    }
}