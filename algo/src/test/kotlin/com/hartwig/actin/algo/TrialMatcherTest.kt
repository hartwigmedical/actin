package com.hartwig.actin.algo

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory.createCurrentDateProvider
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory
import org.junit.Assert
import org.junit.Test

class TrialMatcherTest {
    @Test
    fun canMatchTrialsOnProperTestData() {
        val patient = TestDataFactory.createProperTestPatientRecord()
        val trial = TestTreatmentFactory.createProperTestTrial()
        val matcher = TrialMatcher(createTestEvaluationFunctionFactory())
        val matches = matcher.determineEligibility(patient, listOf(trial))
        Assert.assertEquals(1, matches.size.toLong())
        assertTrialMatch(matches[0])
    }

    @Test
    fun canDeterminePotentialEligibility() {
        val evaluations: MutableList<Evaluation> = mutableListOf()
        evaluations.add(EvaluationTestFactory.withResult(EvaluationResult.PASS))
        Assert.assertTrue(TrialMatcher.isPotentiallyEligible(evaluations))
        evaluations.add(
            ImmutableEvaluation.builder()
                .from(EvaluationTestFactory.withResult(EvaluationResult.FAIL))
                .recoverable(true)
                .build()
        )
        Assert.assertTrue(TrialMatcher.isPotentiallyEligible(evaluations))
        evaluations.add(
            ImmutableEvaluation.builder()
                .from(EvaluationTestFactory.withResult(EvaluationResult.FAIL))
                .recoverable(false)
                .build()
        )
        Assert.assertFalse(TrialMatcher.isPotentiallyEligible(evaluations))
    }

    companion object {
        private fun createTestEvaluationFunctionFactory(): EvaluationFunctionFactory {
            return EvaluationFunctionFactory.create(
                TestDoidModelFactory.createMinimalTestDoidModel(),
                createCurrentDateProvider(),
                TreatmentDatabase(emptyMap(), emptyMap())
            )
        }

        private fun assertTrialMatch(trialMatch: TrialMatch) {
            Assert.assertEquals(1, trialMatch.evaluations().size.toLong())
            Assert.assertTrue(trialMatch.isPotentiallyEligible)
            Assert.assertEquals(
                EvaluationResult.PASS,
                findEvaluationResultForRule(trialMatch.evaluations(), EligibilityRule.IS_AT_LEAST_X_YEARS_OLD)
            )
            Assert.assertEquals(3, trialMatch.cohorts().size.toLong())
            val cohortA = findCohort(trialMatch.cohorts(), "A")
            Assert.assertEquals(1, cohortA.evaluations().size.toLong())
            Assert.assertFalse(cohortA.isPotentiallyEligible)
            Assert.assertEquals(EvaluationResult.FAIL, findEvaluationResultForRule(cohortA.evaluations(), EligibilityRule.NOT))
            val cohortB = findCohort(trialMatch.cohorts(), "B")
            Assert.assertTrue(cohortB.isPotentiallyEligible)
            Assert.assertEquals(
                EvaluationResult.UNDETERMINED,
                findEvaluationResultForRule(cohortB.evaluations(), EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS)
            )
            val cohortC = findCohort(trialMatch.cohorts(), "C")
            Assert.assertTrue(cohortC.isPotentiallyEligible)
            Assert.assertTrue(cohortC.evaluations().isEmpty())
        }

        private fun findEvaluationResultForRule(
            evaluations: Map<Eligibility, Evaluation>,
            ruleToFind: EligibilityRule
        ): EvaluationResult {
            for ((key, value) in evaluations) {
                if (key.function().rule() == ruleToFind) {
                    return value.result()
                }
            }
            throw IllegalStateException("Cannot find evaluation for rule '$ruleToFind'")
        }

        private fun findCohort(cohorts: List<CohortMatch>, cohortIdToFind: String): CohortMatch {
            for (cohort in cohorts) {
                if (cohort.metadata().cohortId() == cohortIdToFind) {
                    return cohort
                }
            }
            throw IllegalStateException("Cannot find cohort with id '$cohortIdToFind'")
        }
    }
}