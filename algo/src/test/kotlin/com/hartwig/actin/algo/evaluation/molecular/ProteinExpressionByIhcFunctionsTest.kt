package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.ihcTest
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

private const val PROTEIN = "PD-L1"

class ProteinExpressionByIhcFunctionsTest {

    private val referenceLevel = 2
    private val limitedFunction = ProteinExpressionByIhcFunctions(PROTEIN, referenceLevel, IhcExpressionComparisonType.LIMITED)
    private val sufficientFunction = ProteinExpressionByIhcFunctions(PROTEIN, referenceLevel, IhcExpressionComparisonType.SUFFICIENT)
    private val exactFunction = ProteinExpressionByIhcFunctions(PROTEIN, referenceLevel, IhcExpressionComparisonType.EXACT)

    @Test
    fun `Should evaluate to undetermined when no IHC tests present in record`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIhcTests(emptyList()))
    }

    @Test
    fun `Should evaluate to undetermined when no IHC test of correct protein present in record`() {
        val test = ihcTest(item = "other", scoreValue = 1.0)
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIhcTests(test))
    }

    @Test
    fun `Should evaluate to undetermined when only score text is provided and exact value is unclear`() {
        val test = MolecularTestFactory.ihcTest(scoreText = "negative")
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIhcTests(test))
    }

    @Test
    fun `Should evaluate to undetermined when exact value is unclear due to comparator`() {
        val test =
            MolecularTestFactory.ihcTest(scoreValue = referenceLevel.toDouble(), scoreValuePrefix = ValueComparison.LARGER_THAN_OR_EQUAL)
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIhcTests(test))
    }

    @Test
    fun `Should fail when only correct IHC test in record has no value`() {
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withIhcTests(ihcTest()))
    }

    @Test
    fun `Should pass when ihc test above requested value in sufficient function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreValue = referenceLevel.plus(1.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, sufficientFunction.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when unclear if above requested value in sufficient function due to comparator`() {
        val test =
            MolecularTestFactory.ihcTest(scoreValue = referenceLevel.minus(1).toDouble(), scoreValuePrefix = ValueComparison.LARGER_THAN)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, sufficientFunction.evaluate(MolecularTestFactory.withIhcTests(test)))
    }

    @Test
    fun `Should pass when ihc test below requested value in limited function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreValue = referenceLevel.minus(1.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, limitedFunction.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when unclear if below requested value in limited function due to comparator`() {
        val test =
            MolecularTestFactory.ihcTest(scoreValue = referenceLevel.plus(1).toDouble(), scoreValuePrefix = ValueComparison.SMALLER_THAN)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, limitedFunction.evaluate(MolecularTestFactory.withIhcTests(test)))
    }

    @Test
    fun `Should pass when ihc test equal to requested value in exact function`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreValue = referenceLevel.toDouble()))
        assertMolecularEvaluation(EvaluationResult.PASS, exactFunction.evaluate(record))
    }

    @Test
    fun `Should fail when prior test contains exact result with prefix`() {
        val priorTest = ihcTest(scoreValuePrefix = ValueComparison.LARGER_THAN, scoreValue = 2.0)
        assertMolecularEvaluation(EvaluationResult.FAIL, exactFunction.evaluate(MolecularTestFactory.withIhcTests(priorTest)))
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        return listOf(limitedFunction, sufficientFunction, exactFunction).forEach {
            assertMolecularEvaluation(expected, it.evaluate(record))
        }
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        ihcTest(item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText)
}