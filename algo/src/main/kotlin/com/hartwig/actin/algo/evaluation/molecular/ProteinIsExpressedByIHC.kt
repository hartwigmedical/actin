package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue

class ProteinIsExpressedByIHC internal constructor(private val protein: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = PriorMolecularTestFunctions.allIHCTestsForProtein(record.clinical().priorMolecularTests(), protein)
        for (ihcTest in ihcTests) {
            var isExpressed = false
            val scoreText = ihcTest.scoreText()
            if (scoreText != null && scoreText.equals("positive", ignoreCase = true)) {
                isExpressed = true
            }
            val scoreValue = ihcTest.scoreValue()
            if (scoreValue != null
                && evaluateVersusMinValue(scoreValue, ihcTest.scoreValuePrefix(), 0.0) == EvaluationResult.PASS
            ) {
                isExpressed = true
            }
            if (isExpressed) {
                return unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Protein $protein has been determined to be expressed (by IHC)")
                    .addPassGeneralMessages("$protein has expression by IHC")
                    .build()
            }
        }
        return if (ihcTests.isNotEmpty()) {
            unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No expression of protein $protein detected by prior IHC test(s)")
                .addFailGeneralMessages("No $protein expression by IHC")
                .build()
        } else {
            unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("No test result found; protein $protein has not been tested by IHC")
                .addUndeterminedGeneralMessages("No $protein IHC test result")
                .build()
        }
    }
}