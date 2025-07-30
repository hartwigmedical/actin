package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest

enum class IhcExpressionComparisonType {
    LIMITED,
    SUFFICIENT,
    EXACT
}

class ProteinExpressionByIhcFunctions(
    private val protein: String, private val referenceExpressionLevel: Int, private val comparisonType: IhcExpressionComparisonType
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestsForItem = IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(record.ihcTests, protein)
        val evaluationsVersusReference = ihcTestsForItem.mapNotNull { ihcTest ->
            ihcTest.scoreValue?.let { scoreValue -> evaluateValue(ihcTest, scoreValue) }
        }.toSet()

        val hasPositiveOrNegativeResult = IhcTestEvaluation.create(protein, record.ihcTests).hasCertainPositiveIhcTestResultsForItem() ||
                IhcTestEvaluation.create(protein, record.ihcTests).hasCertainNegativeIhcTestResultsForItem()

        val comparisonText = when (comparisonType) {
            IhcExpressionComparisonType.LIMITED -> "at most"
            IhcExpressionComparisonType.SUFFICIENT -> "at least"
            IhcExpressionComparisonType.EXACT -> "exactly"
        }

        return when {
            ihcTestsForItem.isEmpty() -> {
                EvaluationFactory.undetermined("No $protein IHC test result", isMissingMolecularResultForEvaluation = true)
            }

            EvaluationResult.PASS in evaluationsVersusReference -> {
                EvaluationFactory.pass("$protein has expression of $comparisonText $referenceExpressionLevel by IHC")
            }

            EvaluationResult.UNDETERMINED in evaluationsVersusReference || hasPositiveOrNegativeResult -> {
                EvaluationFactory.undetermined("Undetermined if $protein expression is $comparisonText $referenceExpressionLevel by IHC")
            }

            else -> EvaluationFactory.fail("$protein expression not $comparisonText $referenceExpressionLevel by IHC")
        }
    }

    private fun evaluateValue(ihcTest: IhcTest, scoreValue: Double): EvaluationResult {
        return when (comparisonType) {
            IhcExpressionComparisonType.SUFFICIENT -> {
                evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, referenceExpressionLevel.toDouble())
            }

            IhcExpressionComparisonType.EXACT -> {
                when (referenceExpressionLevel.toLong() == Math.round(scoreValue) && ihcTest.scoreValuePrefix.isNullOrEmpty()) {
                    true -> EvaluationResult.PASS
                    false -> EvaluationResult.FAIL
                }
            }

            IhcExpressionComparisonType.LIMITED -> {
                evaluateVersusMaxValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix, referenceExpressionLevel.toDouble())
            }
        }
    }
}