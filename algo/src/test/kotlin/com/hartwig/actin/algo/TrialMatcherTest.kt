package com.hartwig.actin.algo

import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationTestFactory
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.StringParameter
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.trial.input.ruleAsEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

data class CombinableMessage(val combineKey: String, val message: String) : EvaluationMessage {
    override fun combineBy(): String {
        return combineKey
    }

    override fun combine(other: EvaluationMessage): EvaluationMessage {
        if (other is CombinableMessage)
            return CombinableMessage(combineKey, listOf(message, other.message).joinToString())
        throw IllegalArgumentException()
    }

    override fun toString(): String {
        return message
    }
}

class TrialMatcherTest {
    private val patient = TestPatientFactory.createProperTestPatientRecord()
    private val trial = TestTrialFactory.createProperTestTrial()
    private val matcher = TrialMatcher(createTestEvaluationFunctionFactory())

    @Test
    fun `Should match trials on proper test data`() {
        val matches = matcher.determineEligibility(patient, listOf(trial))
        assertThat(matches).hasSize(1)
        assertTrialMatch(matches[0])
    }

    @Test
    fun `Should warn when patient has previously participated in matched trial`() {
        val patientWithPreviousTrialParticipation = patient.copy(
            oncologicalHistory = patient.oncologicalHistory + treatmentHistoryEntry(
                isTrial = true, trialAcronym = trial.identification.acronym
            )
        )
        val matches = matcher.determineEligibility(patientWithPreviousTrialParticipation, listOf(trial))
        assertThat(matches).hasSize(1)
        assertThat(findEvaluationResultForRule(matches.first().evaluations, EligibilityRule.WARN_IF)).isEqualTo(EvaluationResult.WARN)
    }

    @Test
    fun `Should determine potential eligibility`() {
        val evaluations = listOf(EvaluationTestFactory.withResult(EvaluationResult.PASS))
        assertThat(TrialMatcher.isPotentiallyEligible(evaluations)).isTrue

        val recoverableFail = EvaluationTestFactory.withResult(EvaluationResult.FAIL).copy(recoverable = true)
        assertThat(TrialMatcher.isPotentiallyEligible(evaluations + recoverableFail)).isTrue

        val unrecoverableFail = EvaluationTestFactory.withResult(EvaluationResult.FAIL).copy(recoverable = false)
        assertThat(TrialMatcher.isPotentiallyEligible(evaluations + unrecoverableFail)).isFalse
    }

    @Test
    fun `Should return the same number of cohorts`() {
        val matches = matcher.determineEligibility(patient, listOf(trial))
        assertThat(matches.sumOf { it.cohorts.size + it.nonEvaluableCohorts.size }).isEqualTo(trial.cohorts.size)
    }

    @Test
    fun `Should combine messages in evaluation when keys match`() {
        val messageSet = setOf(CombinableMessage("key", "test1"), CombinableMessage("key", "test2"))
        val evaluation = EvaluationTestFactory.withResult(EvaluationResult.PASS)
            .copy(passMessages = messageSet, warnMessages = messageSet, undeterminedMessages = messageSet, failMessages = messageSet)
            .combineMessages()
        val expectedMessage = "test1, test2"
        assertThat(evaluation.passMessagesStrings()).containsOnly(expectedMessage)
        assertThat(evaluation.warnMessagesStrings()).containsOnly(expectedMessage)
        assertThat(evaluation.failMessagesStrings()).containsOnly(expectedMessage)
        assertThat(evaluation.undeterminedMessagesStrings()).containsOnly(expectedMessage)
    }

    @Test
    fun `Should not combine messages in evaluation when keys don't match`() {
        val messageSet = setOf(CombinableMessage("key1", "test1"), CombinableMessage("key2", "test2"))
        val evaluation = EvaluationTestFactory.withResult(EvaluationResult.PASS)
            .copy(passMessages = messageSet, warnMessages = messageSet, undeterminedMessages = messageSet, failMessages = messageSet)
            .combineMessages()
        val expectedMessage = setOf("test1", "test2")
        assertThat(evaluation.passMessagesStrings()).isEqualTo(expectedMessage)
        assertThat(evaluation.warnMessagesStrings()).isEqualTo(expectedMessage)
        assertThat(evaluation.failMessagesStrings()).isEqualTo(expectedMessage)
        assertThat(evaluation.undeterminedMessagesStrings()).isEqualTo(expectedMessage)
    }

    companion object {
        private fun createTestEvaluationFunctionFactory(): EvaluationFunctionFactory {
            return EvaluationFunctionFactory.create(RuleMappingResourcesTestFactory.create())
        }

        private fun assertTrialMatch(trialMatch: TrialMatch) {
            assertThat(trialMatch.evaluations).hasSize(2)
            assertThat(trialMatch.isPotentiallyEligible).isTrue
            assertThat(findEvaluationResultForRule(trialMatch.evaluations, EligibilityRule.IS_AT_LEAST_X_YEARS_OLD))
                .isEqualTo(EvaluationResult.PASS)
            assertThat(findEvaluationResultForRule(trialMatch.evaluations, EligibilityRule.WARN_IF)).isEqualTo(EvaluationResult.PASS)
            assertThat(trialMatch.cohorts).hasSize(3)

            val cohortA = findCohort(trialMatch.cohorts, "A")
            assertThat(cohortA.evaluations).hasSize(1)
            assertThat(cohortA.isPotentiallyEligible).isFalse
            assertThat(findEvaluationResultForRule(cohortA.evaluations, EligibilityRule.NOT)).isEqualTo(EvaluationResult.FAIL)

            val cohortB = findCohort(trialMatch.cohorts, "B")
            assertThat(cohortB.isPotentiallyEligible).isTrue
            assertThat(findEvaluationResultForRule(cohortB.evaluations, EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS)).isEqualTo(
                EvaluationResult.NOT_EVALUATED
            )

            val cohortC = findCohort(trialMatch.cohorts, "C")
            assertThat(cohortC.isPotentiallyEligible).isTrue
            assertThat(cohortC.evaluations).isEmpty()
        }

        private fun findEvaluationResultForRule(evaluations: Map<Eligibility, Evaluation>, ruleToFind: EligibilityRule): EvaluationResult {
            val match = evaluations.entries.find { (key, _) -> key.function.ruleAsEnum() == ruleToFind }
                ?: throw IllegalStateException("Cannot find evaluation for rule '$ruleToFind'")
            return match.value.result
        }

        private fun findCohort(cohorts: List<CohortMatch>, cohortIdToFind: String): CohortMatch {
            return cohorts.find { it.metadata.cohortId == cohortIdToFind }
                ?: throw IllegalStateException("Cannot find cohort with id '$cohortIdToFind'")
        }
    }
}
