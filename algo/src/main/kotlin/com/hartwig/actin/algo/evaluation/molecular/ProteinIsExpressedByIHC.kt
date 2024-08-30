package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorIHCTest

class ProteinIsExpressedByIHC internal constructor(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = PriorIHCTestFunctions.allIHCTestsForProtein(record.priorIHCTests, protein)

        return when {
            ihcTests.any { ihcTest -> ihcTest.scoreText?.lowercase() == "positive" || testScoredAboveZero(ihcTest) } -> {
                EvaluationFactory.pass("Protein $protein is expressed according to IHC", "$protein has expression by IHC")
            }

            ihcTests.isNotEmpty() -> {
                EvaluationFactory.fail(
                    "No expression of protein $protein detected by prior IHC test(s)", "No $protein expression by IHC"
                )
            }

            else -> {
                EvaluationFactory.undetermined(
                    "No test result found; protein $protein has not been tested by IHC", "No $protein IHC test result"
                )
            }
        }
    }

    private fun testScoredAboveZero(ihcTest: PriorIHCTest) = ihcTest.scoreValue?.let { scoreValue ->
        evaluateVersusMinValue(scoreValue, ihcTest.scoreValuePrefix, 0.0)
    } == EvaluationResult.PASS
}