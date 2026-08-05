package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.clinical.interpretation.asRange
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMinimumLanskyKarnofskyScore internal constructor(
    private val performanceScore: PerformanceScore,
    private val minScore: Int,
    private val labels: EvaluationLabels.General
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val scoreDisplay = performanceScore.display()
        val who = record.performanceStatus.latestWho
            ?: return EvaluationFactory.undetermined(labels.hasMinimumLanskyKarnofskyScoreUndeterminedMissing(scoreDisplay, minScore))
        val whoRange = who.asRange()
        val passScore = toMinScoreForWHO(whoRange.last)
        val undeterminedScore = toMaxScoreForWHO(whoRange.first)
        val warnScore = toMaxScoreForWHO((whoRange.first - 1).coerceAtLeast(0))

        return when {
            passScore >= minScore -> {
                EvaluationFactory.pass(labels.hasMinimumLanskyKarnofskyScorePass(scoreDisplay, minScore))
            }

            undeterminedScore >= minScore -> {
                EvaluationFactory.undetermined(labels.hasMinimumLanskyKarnofskyScoreUndetermined(scoreDisplay, minScore))
            }

            warnScore >= minScore -> {
                EvaluationFactory.recoverableFail(labels.hasMinimumLanskyKarnofskyScoreRecoverableFail(scoreDisplay, minScore))
            }

            else -> EvaluationFactory.fail(labels.hasMinimumLanskyKarnofskyScoreFail(scoreDisplay, minScore))
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