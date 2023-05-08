package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue

class ProteinHasSufficientExpressionByIHC internal constructor(private val protein: String, private val minExpressionLevel: Int) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTests = PriorMolecularTestFunctions.allIHCTestsForProtein(record.clinical().priorMolecularTests(), protein)
        var mightMeetMinExpressionLevelByIHC = false
        for (ihcTest in ihcTests) {
            val scoreValue = ihcTest.scoreValue()
            if (scoreValue != null) {
                // We assume IHC prior molecular tests always have integer score values.
                val evaluation =
                    evaluateVersusMinValue(Math.round(scoreValue).toDouble(), ihcTest.scoreValuePrefix(), minExpressionLevel.toDouble())
                if (evaluation == EvaluationResult.PASS) {
                    return unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(
                            "Protein $protein has expression level of at least $minExpressionLevel (by IHC)"
                        )
                        .addPassGeneralMessages("Adequate $protein IHC expression level")
                        .build()
                } else if (evaluation == EvaluationResult.UNDETERMINED) {
                    mightMeetMinExpressionLevelByIHC = true
                }
            }
            val scoreText = ihcTest.scoreText()
            if (scoreText != null && (scoreText.equals("positive", ignoreCase = true) || scoreText.equals("negative", ignoreCase = true))) {
                mightMeetMinExpressionLevelByIHC = true
            }
        }
        if (mightMeetMinExpressionLevelByIHC) {
            return unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    "Unknown if protein $protein expression level is at least $minExpressionLevel (by IHC)"
                )
                .addUndeterminedGeneralMessages("Unknown $protein exact IHC expression level")
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
            .addFailSpecificMessages(
                "Protein $protein does not meet required expression level $minExpressionLevel (by IHC)"
            )
            .addFailGeneralMessages("Insufficient " + protein + "exact IHC expression level")
            .build()
    }
}