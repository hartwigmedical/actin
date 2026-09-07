package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.ihcTest
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

private const val PROTEIN = "PD-L1"

class ProteinExpressionByIhcFunctionsTest {

    private val referenceLevel = 2
    private val limitedFunction = ProteinExpressionByIhcFunctions(PROTEIN, referenceLevel, IhcExpressionComparisonType.LIMITED)
    private val sufficientFunction = ProteinExpressionByIhcFunctions(PROTEIN, referenceLevel, IhcExpressionComparisonType.SUFFICIENT)
    private val exactFunction = ProteinExpressionByIhcFunctions(PROTEIN, referenceLevel, IhcExpressionComparisonType.EXACT)

    @Test
    fun `Should be undetermined when no IHC tests present in record`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIhcTests(emptyList())) { "No PD-L1 IHC test result" }
    }

    @Test
    fun `Should be undetermined when no IHC test of correct protein present in record`() {
        val test = ihcTest(item = "other", scoreLowerBound = 1.0, scoreUpperBound = 1.0)
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIhcTests(test)) { "No PD-L1 IHC test result" }
    }

    @Test
    fun `Should warn when only score text is provided and exact value is unclear`() {
        val test = ihcTest(scoreText = "negative")
        evaluateFunctions(EvaluationResult.WARN, MolecularTestFactory.withIhcTests(test)) { comparison ->
            "Undetermined if PD-L1 expression is $comparison 2 by IHC"
        }
    }

    @ParameterizedTest
    @CsvSource("2.0,", "2.0, 2.0")
    fun `Should pass in sufficient function when lower bound is at requested value`(
        lower: Double, upper: String?
    ) {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = lower, scoreUpperBound = upper?.toDouble()))
        assertMolecularEvaluation(EvaluationResult.PASS, sufficientFunction.evaluate(record), "PD-L1 has expression of at least 2 by IHC")
    }

    @Test
    fun `Should warn when exact value is unclear due to bounds in limited function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = referenceLevel.toDouble()))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            limitedFunction.evaluate(record),
            "Undetermined if PD-L1 expression is at most 2 by IHC"
        )
    }

    @Test
    fun `Should fail when exact value is unclear due to bounds in exact function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = referenceLevel.toDouble()))
        assertMolecularEvaluation(EvaluationResult.FAIL, exactFunction.evaluate(record), "PD-L1 expression not exactly 2 by IHC")
    }

    @Test
    fun `Should fail when only correct IHC test in record has no value`() {
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withIhcTests(ihcTest())) { comparison ->
            "PD-L1 expression not $comparison 2 by IHC"
        }
    }

    @Test
    fun `Should pass when ihc test above requested value in sufficient function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = referenceLevel.plus(1.0), scoreUpperBound = referenceLevel.plus(1.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, sufficientFunction.evaluate(record), "PD-L1 has expression of at least 2 by IHC")
    }

    @ParameterizedTest
    @CsvSource("1.0,", "1.0, 3.0")
    fun `Should warn in sufficient function when unclear if above requested value due to bounds`(
        lower: Double, upper: String?
    ) {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = lower, scoreUpperBound = upper?.toDouble()))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            sufficientFunction.evaluate(record),
            "Undetermined if PD-L1 expression is at least 2 by IHC"
        )
    }

    @Test
    fun `Should pass when ihc test below requested value in limited function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = referenceLevel.minus(1.0), scoreUpperBound = referenceLevel.minus(1.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, limitedFunction.evaluate(record), "PD-L1 has expression of at most 2 by IHC")
    }

    @ParameterizedTest
    @CsvSource(", 3.0", "1.0, 3.0")
    fun `Should warn in limited function when unclear if below requested value due to bounds`(
        lower: String?, upper: Double
    ) {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = lower?.toDouble(), scoreUpperBound = upper))
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            limitedFunction.evaluate(record),
            "Undetermined if PD-L1 expression is at most 2 by IHC"
        )
    }

    @Test
    fun `Should pass when ihc test equal to requested value in exact function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = referenceLevel.toDouble(), scoreUpperBound = referenceLevel.toDouble()))
        assertMolecularEvaluation(EvaluationResult.PASS, exactFunction.evaluate(record), "PD-L1 has expression of exactly 2 by IHC")
    }

    @Test
    fun `Should fail when prior test contains value with only lower bound (not exact)`() {
        val priorTest = ihcTest(scoreLowerBound = 2.0)
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            exactFunction.evaluate(MolecularTestFactory.withIhcTests(priorTest)),
            "PD-L1 expression not exactly 2 by IHC"
        )
    }

    @Test
    fun `Should fail when ihc test below requested value in sufficient function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = 0.0, scoreUpperBound = 1.0))
        assertMolecularEvaluation(EvaluationResult.FAIL, sufficientFunction.evaluate(record), "PD-L1 expression not at least 2 by IHC")
    }

    @Test
    fun `Should pass when ihc test at requested value in limited function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = referenceLevel.toDouble(), scoreUpperBound = referenceLevel.toDouble()))
        assertMolecularEvaluation(EvaluationResult.PASS, limitedFunction.evaluate(record), "PD-L1 has expression of at most 2 by IHC")
    }

    @Test
    fun `Should fail when ihc test above requested value in limited function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = 3.0, scoreUpperBound = 3.0))
        assertMolecularEvaluation(EvaluationResult.FAIL, limitedFunction.evaluate(record), "PD-L1 expression not at most 2 by IHC")
    }

    @Test
    fun `Should fail when ihc test does not match requested value in exact function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = 1.0, scoreUpperBound = 1.0))
        assertMolecularEvaluation(EvaluationResult.FAIL, exactFunction.evaluate(record), "PD-L1 expression not exactly 2 by IHC")
    }

    @Test
    fun `Should fail when ihc test contains range instead of exact value in exact function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = 1.0, scoreUpperBound = 3.0))
        assertMolecularEvaluation(EvaluationResult.FAIL, exactFunction.evaluate(record), "PD-L1 expression not exactly 2 by IHC")
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord, expectedMessage: (String) -> String) {
        return listOf(limitedFunction to "at most", sufficientFunction to "at least", exactFunction to "exactly")
            .forEach { (function, comparison) ->
                assertMolecularEvaluation(expected, function.evaluate(record), expectedMessage(comparison))
            }
    }

    private fun ihcTest(scoreLowerBound: Double? = null, scoreUpperBound: Double? = null, scoreText: String? = null) =
        ihcTest(item = PROTEIN, scoreLowerBound = scoreLowerBound, scoreUpperBound = scoreUpperBound, scoreText = scoreText)
}
