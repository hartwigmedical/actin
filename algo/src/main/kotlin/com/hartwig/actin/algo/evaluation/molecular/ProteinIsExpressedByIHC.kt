package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import java.time.LocalDate

class ProteinIsExpressedByIHC(private val protein: String,  private val gene: String, private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = IhcTestFilter.allIHCTestsForProtein(record.priorIHCTests, protein)

        return when {
            ihcTests.any { ihcTest -> ihcTest.scoreText?.lowercase() == "positive" || testScoredAboveZero(ihcTest) } -> {
                EvaluationFactory.pass("$protein has expression by IHC")
            }

            ihcTests.isNotEmpty() -> {
                EvaluationFactory.fail("No $protein expression by IHC")
            }

            else -> {
                val additionalMessage = IHCMessagesFunctions.additionalMessageWhenGeneIsWildType(gene, record, maxTestAge)
                EvaluationFactory.undetermined("No $protein IHC test result$additionalMessage", isMissingMolecularResultForEvaluation = true)
            }
        }
    }

    private fun testScoredAboveZero(ihcTest: PriorIHCTest) = ihcTest.scoreValue?.let { scoreValue ->
        evaluateVersusMinValue(scoreValue, ihcTest.scoreValuePrefix, 0.0)
    } == EvaluationResult.PASS
}