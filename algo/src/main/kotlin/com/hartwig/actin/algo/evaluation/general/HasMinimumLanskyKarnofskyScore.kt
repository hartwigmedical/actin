package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasMinimumLanskyKarnofskyScore internal constructor(private val performanceScore: PerformanceScore, private val minScore: Int) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinical().clinicalStatus().who()
            ?: return recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    "Cannot evaluate " + performanceScore.display() + " performance score because WHO is missing"
                )
                .addUndeterminedGeneralMessages("Missing " + performanceScore.display() + " score")
                .build()
        val passScore = toMinScoreForWHO(who)
        val undeterminedScore = toMaxScoreForWHO(who)
        val warnScore = toMaxScoreForWHO((who - 1).coerceAtLeast(0))
        return when {
            passScore >= minScore -> {
                unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(performanceScore.display() + " score based on WHO score is at least " + minScore)
                    .addPassGeneralMessages("Minimum " + performanceScore.display() + " requirements")
                    .build()
            }

            undeterminedScore >= minScore -> {
                unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                        "Not clear whether " + performanceScore.display() + " score based on WHO score is at least " + minScore
                    )
                    .addUndeterminedSpecificMessages("Undetermined minimum " + performanceScore.display() + " requirements")
                    .build()
            }

            warnScore >= minScore -> {
                unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(performanceScore.display() + " score based on WHO score exceeds requested score of " + minScore)
                    .addWarnSpecificMessages("Minimum " + performanceScore.display() + " requirements")
                    .build()
            }

            else -> unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages(performanceScore.display() + " score based on WHO score is below " + minScore)
                .addFailGeneralMessages("Minimum " + performanceScore.display() + " requirements")
                .build()
        }
    }

    companion object {
        private fun toMinScoreForWHO(who: Int): Int {
            return when (who) {
                0 -> 100
                1 -> 80
                2 -> 60
                3 -> 40
                4 -> 10
                5 -> 0
                else -> throw IllegalStateException("Illegal who status: $who")
            }
        }

        private fun toMaxScoreForWHO(who: Int): Int {
            return when (who) {
                0 -> 100
                1 -> 90
                2 -> 70
                3 -> 50
                4 -> 30
                5 -> 0
                else -> throw IllegalStateException("Illegal who status: $who")
            }
        }
    }
}