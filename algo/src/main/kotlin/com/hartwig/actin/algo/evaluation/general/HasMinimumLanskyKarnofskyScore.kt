package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision

class HasMinimumLanskyKarnofskyScore internal constructor(private val performanceScore: PerformanceScore, private val minScore: Int) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val who = record.performanceStatus.latestWho
            ?: return EvaluationFactory.undetermined(
                "Undetermined if ${performanceScore.display()} score based on WHO status is at least $minScore (WHO data missing)"
            )
        val whoRange = getWhoRange(who)
        val passScore = toMinScoreForWHO(whoRange.last)
        val undeterminedScore = toMaxScoreForWHO(whoRange.first)
        val warnScore = toMaxScoreForWHO((whoRange.first - 1).coerceAtLeast(0))
        return when {
            passScore >= minScore -> {
                EvaluationFactory.pass("${performanceScore.display()} score based on WHO status is at least $minScore")
            }

            undeterminedScore >= minScore -> {
                EvaluationFactory.undetermined("Undetermined if ${performanceScore.display()} score is at least $minScore")
            }

            warnScore >= minScore -> {
                EvaluationFactory.warn("${performanceScore.display()} score based on WHO status exceeds requested score of $minScore")
            }

            else -> EvaluationFactory.fail("${performanceScore.display()} score based on WHO status is below $minScore")
        }
    }

    private fun getWhoRange(who: WhoStatus): IntRange {
        return when (who.precision) {
            WhoStatusPrecision.EXACT -> who.status..who.status
            WhoStatusPrecision.AT_MOST -> 0..who.status
            WhoStatusPrecision.AT_LEAST -> who.status..5
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