package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMinimumLanskyKarnofskyScore internal constructor(private val performanceScore: PerformanceScore, private val minScore: Int) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.clinicalStatus.who ?: return EvaluationFactory.recoverableUndetermined(
            "Cannot evaluate ${performanceScore.display()} performance score because WHO is missing",
            "Missing ${performanceScore.display()} score"
        )
        val passScore = toMinScoreForWHO(who)
        val undeterminedScore = toMaxScoreForWHO(who)
        val warnScore = toMaxScoreForWHO((who - 1).coerceAtLeast(0))
        return when {
            passScore >= minScore -> {
                EvaluationFactory.pass(
                    "${performanceScore.display()} score based on WHO score is at least $minScore",
                    "Minimum ${performanceScore.display()} requirements"
                )
            }

            undeterminedScore >= minScore -> {
                EvaluationFactory.undetermined(
                    "Not clear whether ${performanceScore.display()} score based on WHO score is at least $minScore",
                    "Undetermined minimum ${performanceScore.display()} requirements"
                )
            }

            warnScore >= minScore -> {
                EvaluationFactory.warn(
                    "${performanceScore.display()} score based on WHO score exceeds requested score of $minScore",
                    "Minimum ${performanceScore.display()} requirements"
                )
            }

            else -> EvaluationFactory.fail(
                "${performanceScore.display()} score based on WHO score is below $minScore",
                "Minimum ${performanceScore.display()} requirements"
            )
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