package com.hartwig.actin.treatment.trial

import com.google.common.io.Resources
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.treatment.trial.TrialFactory.Companion.create
import com.hartwig.actin.treatment.trial.config.TestTrialConfigFactory
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class TrialFactoryTest {
    @Test
    @Throws(IOException::class)
    fun canCreateFromTrialConfigDirectory() {
        Assert.assertNotNull(
            create(
                TRIAL_CONFIG_DIRECTORY,
                TestDoidModelFactory.createMinimalTestDoidModel(),
                TestGeneFilterFactory.createNeverValid()
            )
        )
    }

    @Test
    fun canCreateFromProperTestModel() {
        val factory = TrialFactory(
            TrialConfigModel(TestTrialConfigFactory.createProperTestTrialConfigDatabase()),
            TestEligibilityFactoryFactory.createTestEligibilityFactory()
        )
        val trials = factory.create()
        Assert.assertEquals(2, trials.size.toLong())
        val trial = findTrial(trials, "TEST-1")
        Assert.assertTrue(trial.identification().open())
        Assert.assertEquals("Acronym-TEST-1", trial.identification().acronym())
        Assert.assertEquals("Title for TEST-1", trial.identification().title())
        Assert.assertEquals(1, trial.generalEligibility().size.toLong())
        val generalFunction = findFunction(trial.generalEligibility(), EligibilityRule.IS_AT_LEAST_X_YEARS_OLD)
        Assert.assertEquals(1, generalFunction.parameters().size.toLong())
        Assert.assertEquals(3, trial.cohorts().size.toLong())
        val cohortA = findCohort(trial.cohorts(), "A")
        Assert.assertEquals("Cohort A", cohortA.metadata().description())
        Assert.assertEquals(2, cohortA.eligibility().size.toLong())
        val cohortFunction1 = findFunction(cohortA.eligibility(), EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        Assert.assertEquals(1, cohortFunction1.parameters().size.toLong())
        Assert.assertTrue(cohortFunction1.parameters().contains("1"))
        val cohortFunction2 = findFunction(cohortA.eligibility(), EligibilityRule.NOT)
        Assert.assertEquals(1, cohortFunction1.parameters().size.toLong())
        val subFunction = cohortFunction2.parameters()[0] as EligibilityFunction
        Assert.assertEquals(EligibilityRule.OR, subFunction.rule())
        Assert.assertEquals(2, subFunction.parameters().size.toLong())
        val cohortB = findCohort(trial.cohorts(), "B")
        Assert.assertEquals("Cohort B", cohortB.metadata().description())
        Assert.assertTrue(cohortB.eligibility().isEmpty())
        val cohortC = findCohort(trial.cohorts(), "C")
        Assert.assertEquals("Cohort C", cohortC.metadata().description())
        Assert.assertTrue(cohortC.eligibility().isEmpty())
    }

    companion object {
        private val TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").path
        private fun findTrial(trials: List<Trial>, trialId: String): Trial {
            for (trial in trials) {
                if (trial.identification().trialId() == trialId) {
                    return trial
                }
            }
            throw IllegalStateException("Could not find trial with ID: $trialId")
        }

        private fun findCohort(cohorts: List<Cohort>, cohortId: String): Cohort {
            for (cohort in cohorts) {
                if (cohort.metadata().cohortId() == cohortId) {
                    return cohort
                }
            }
            throw IllegalStateException("Could not find cohort with ID: $cohortId")
        }

        private fun findFunction(eligibility: List<Eligibility>, rule: EligibilityRule): EligibilityFunction {
            for (entry in eligibility) {
                if (entry.function().rule() == rule) {
                    return entry.function()
                }
            }
            throw IllegalStateException("Could not find eligibility function with rule: $rule")
        }
    }
}