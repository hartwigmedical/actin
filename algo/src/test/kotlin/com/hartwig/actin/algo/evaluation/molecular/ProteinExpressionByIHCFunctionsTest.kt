package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.priorIHCTest
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

private const val IHC = "IHC"
private const val PROTEIN = "PD-L1"
private const val GENE = "PD-L1"

class ProteinExpressionByIHCFunctionsTest {

    private val referenceLevel = 2
    private val limitedFunction = ProteinExpressionByIHCFunctions(PROTEIN, GENE, referenceLevel, IhcExpressionComparisonType.LIMITED)
    private val sufficientFunction = ProteinExpressionByIHCFunctions(PROTEIN, GENE, referenceLevel, IhcExpressionComparisonType.SUFFICIENT)
    private val exactFunction = ProteinExpressionByIHCFunctions(PROTEIN, GENE, referenceLevel, IhcExpressionComparisonType.EXACT)

    @Test
    fun `Should evaluate to undetermined when no IHC tests present in record`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIHCTests(emptyList()))
    }

    @Test
    fun `Should evaluate to undetermined when no IHC test of correct protein present in record`() {
        val test = priorIHCTest(test = IHC, item = "other", scoreValue = 1.0)
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIHCTests(test))
    }

    @Test
    fun `Should evaluate to undetermined when only score text is provided and exact value is unclear`() {
        val test = priorIHCTest(scoreText = "negative")
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIHCTests(test))
    }

    @Test
    fun `Should evaluate to undetermined when exact value is unclear due to comparator`() {
        val test = priorIHCTest(scoreValue = referenceLevel.toDouble(), scoreValuePrefix = ValueComparison.LARGER_THAN_OR_EQUAL)
        evaluateFunctions(EvaluationResult.UNDETERMINED, MolecularTestFactory.withIHCTests(test))
    }

    @Test
    fun `Should fail when only correct IHC test in record has no value`() {
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withIHCTests(ihcTest()))
    }

    @Test
    fun `Should pass when ihc test above requested value in sufficient function`() {
        val record = MolecularTestFactory.withIHCTests(ihcTest(scoreValue = referenceLevel.plus(1.0)))
        assertEvaluation(EvaluationResult.PASS, sufficientFunction.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when unclear if above requested value in sufficient function due to comparator`() {
        val test = priorIHCTest(scoreValue = referenceLevel.minus(1).toDouble(), scoreValuePrefix = ValueComparison.LARGER_THAN)
        assertEvaluation(EvaluationResult.UNDETERMINED, sufficientFunction.evaluate(MolecularTestFactory.withIHCTests(test)))
    }

    @Test
    fun `Should pass when ihc test below requested value in limited function`() {
        val record = MolecularTestFactory.withIHCTests(ihcTest(scoreValue = referenceLevel.minus(1.0)))
        assertEvaluation(EvaluationResult.PASS, limitedFunction.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when unclear if below requested value in limited function due to comparator`() {
        val test = priorIHCTest(scoreValue = referenceLevel.plus(1).toDouble(), scoreValuePrefix = ValueComparison.SMALLER_THAN)
        assertEvaluation(EvaluationResult.UNDETERMINED, limitedFunction.evaluate(MolecularTestFactory.withIHCTests(test)))
    }

    @Test
    fun `Should pass when ihc test equal to requested value in exact function`() {
        val record = MolecularTestFactory.withIHCTests(ihcTest(scoreValue = referenceLevel.toDouble()))
        assertEvaluation(EvaluationResult.PASS, exactFunction.evaluate(record))
    }

    @Test
    fun `Should fail when prior test contains exact result with prefix`() {
        val priorTest = ihcTest(scoreValuePrefix = ValueComparison.LARGER_THAN, scoreValue = 2.0)
        assertEvaluation(EvaluationResult.FAIL, exactFunction.evaluate(MolecularTestFactory.withIHCTests(priorTest)))
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        return listOf(limitedFunction, sufficientFunction, exactFunction).forEach { assertEvaluation(expected, it.evaluate(record)) }
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        priorIHCTest(test = IHC, item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText)
}