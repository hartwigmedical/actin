package com.hartwig.actin.algo

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResourcesTestFactory
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.TestTrialFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialMatcherTest {

    @Test
    fun `Should match trials on proper test data`() {
        val patient = TestPatientFactory.createProperTestPatientRecord()
        val trial = TestTrialFactory.createProperTestTrial()
        val matcher = TrialMatcher(createTestEvaluationFunctionFactory())
        val matches = matcher.determineEligibility(patient, listOf(trial))
        assertThat(matches).hasSize(1)
        assertTrialMatch(matches[0])
    }

    @Test
    fun `Should determine potential eligibility`() {
        val evaluations = mutableListOf(EvaluationTestFactory.withResult(EvaluationResult.PASS))
        assertThat(TrialMatcher.isPotentiallyEligible(evaluations)).isTrue
        evaluations.add(EvaluationTestFactory.withResult(EvaluationResult.FAIL).copy(recoverable = true))
        assertThat(TrialMatcher.isPotentiallyEligible(evaluations)).isTrue
        evaluations.add(EvaluationTestFactory.withResult(EvaluationResult.FAIL).copy(recoverable = false))
        assertThat(TrialMatcher.isPotentiallyEligible(evaluations)).isFalse
    }

    companion object {
        private fun createTestEvaluationFunctionFactory(): EvaluationFunctionFactory {
            return EvaluationFunctionFactory.create(RuleMappingResourcesTestFactory.create())
        }

        private fun assertTrialMatch(trialMatch: TrialMatch) {
            assertThat(trialMatch.evaluations).hasSize(1)
            assertThat(trialMatch.isPotentiallyEligible).isTrue
            assertThat(findEvaluationResultForRule(trialMatch.evaluations, EligibilityRule.IS_AT_LEAST_X_YEARS_OLD))
                .isEqualTo(EvaluationResult.PASS)
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
            val match = evaluations.entries.find { (key, _) -> key.function.rule == ruleToFind }
                ?: throw IllegalStateException("Cannot find evaluation for rule '$ruleToFind'")
            return match.value.result
        }

        private fun findCohort(cohorts: List<CohortMatch>, cohortIdToFind: String): CohortMatch {
            return cohorts.find { it.metadata.cohortId == cohortIdToFind }
                ?: throw IllegalStateException("Cannot find cohort with id '$cohortIdToFind'")
        }
    }
}