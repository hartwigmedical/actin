package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateBoundsVersusMaxValue
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateBoundsVersusMinValue
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import kotlin.math.roundToInt

enum class IhcExpressionComparisonType {
    LIMITED,
    SUFFICIENT,
    EXACT
}

class ProteinExpressionByIhcFunctions(
    private val protein: String, private val referenceExpressionLevel: Int, private val comparisonType: IhcExpressionComparisonType
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create(protein, record.ihcTests)

        val evaluationsVersusReference = ihcTestEvaluation.filteredTests.mapNotNull { ihcTest ->
            if (ihcTest.scoreLowerBound != null || ihcTest.scoreUpperBound != null) evaluateValue(ihcTest) else null
        }.toSet()

        val hasPositiveOrNegativeResult =
            ihcTestEvaluation.hasCertainBroadPositiveResultsForItem() || ihcTestEvaluation.hasCertainNegativeResultsForItem()

        val comparisonText = when (comparisonType) {
            IhcExpressionComparisonType.LIMITED -> "at most"
            IhcExpressionComparisonType.SUFFICIENT -> "at least"
            IhcExpressionComparisonType.EXACT -> "exactly"
        }

        return when {
            ihcTestEvaluation.filteredTests.isEmpty() -> {
                EvaluationFactory.undetermined("No $protein IHC test result", isMissingMolecularResultForEvaluation = true)
            }

            EvaluationResult.PASS in evaluationsVersusReference -> {
                EvaluationFactory.pass(
                    "$protein has expression of $comparisonText $referenceExpressionLevel by IHC",
                    inclusionEvents = setOf("IHC $protein expression")
                )
            }

            EvaluationResult.UNDETERMINED in evaluationsVersusReference || hasPositiveOrNegativeResult -> {
                EvaluationFactory.warn(
                    "Undetermined if $protein expression is $comparisonText $referenceExpressionLevel by IHC",
                    inclusionEvents = setOf("Potential IHC $protein expression")
                )
            }

            else -> EvaluationFactory.fail("$protein expression not $comparisonText $referenceExpressionLevel by IHC")
        }
    }

    private fun evaluateValue(ihcTest: IhcTest): EvaluationResult {
        val roundedScoreLowerBound = ihcTest.scoreLowerBound?.roundToInt()
        val roundedScoreUpperBound = ihcTest.scoreUpperBound?.roundToInt()
        return when (comparisonType) {
            IhcExpressionComparisonType.SUFFICIENT -> {
                evaluateBoundsVersusMinValue(roundedScoreLowerBound?.toDouble(), roundedScoreUpperBound?.toDouble(), referenceExpressionLevel.toDouble())
            }

            IhcExpressionComparisonType.EXACT -> {
                when (roundedScoreLowerBound == roundedScoreUpperBound && roundedScoreLowerBound == referenceExpressionLevel) {
                    true -> EvaluationResult.PASS
                    false -> EvaluationResult.FAIL
                }
            }

            IhcExpressionComparisonType.LIMITED -> {
                evaluateBoundsVersusMaxValue(roundedScoreLowerBound?.toDouble(), roundedScoreUpperBound?.toDouble(), referenceExpressionLevel.toDouble())
            }
        }
    }
}