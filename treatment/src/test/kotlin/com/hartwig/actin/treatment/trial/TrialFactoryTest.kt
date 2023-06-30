package com.hartwig.actin.treatment.trial

import com.google.common.io.Resources
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.treatment.ctc.TestCTCModelFactory
import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.treatment.trial.config.TestTrialConfigDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.IOException

class TrialFactoryTest {
    @Test
    @Throws(IOException::class)
    fun canCreateFromTrialConfigDirectory() {
        assertThat(
            TrialFactory.create(
                TRIAL_CONFIG_DIRECTORY,
                TestCTCModelFactory.createWithMinimalTestCTCDatabase(),
                TestDoidModelFactory.createMinimalTestDoidModel(),
                TestGeneFilterFactory.createNeverValid()
            )
        ).isNotNull
    }

    @Test
    fun canCreateFromProperTestModel() {
        val factory = TrialFactory(
            TrialConfigModel.createFromDatabase(TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase()),
            TestCTCModelFactory.createWithProperTestCTCDatabase(),
            TestEligibilityFactoryFactory.createTestEligibilityFactory()
        )
        val trials = factory.createTrials()
        assertThat(trials).hasSize(2)

        val trial = findTrial(trials, "TEST-1")
        assertThat(trial.identification().open()).isTrue
        assertThat(trial.identification().acronym()).isEqualTo("Acronym-TEST-1")
        assertThat(trial.identification().title()).isEqualTo("Title for TEST-1")
        assertThat(trial.generalEligibility()).hasSize(1)

        val generalFunction = findFunction(trial.generalEligibility(), EligibilityRule.IS_AT_LEAST_X_YEARS_OLD)
        assertThat(generalFunction.parameters()).hasSize(1)
        assertThat(trial.cohorts()).hasSize(3)

        val cohortA = findCohort(trial.cohorts(), "A")
        assertThat(cohortA.metadata().description()).isEqualTo("Cohort A")
        assertThat(cohortA.eligibility()).hasSize(2)

        val cohortFunction1 = findFunction(cohortA.eligibility(), EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        assertThat(cohortFunction1.parameters()).containsExactly("1")

        val cohortFunction2 = findFunction(cohortA.eligibility(), EligibilityRule.NOT)
        assertThat(cohortFunction2.parameters()).hasSize(1)

        val subFunction = cohortFunction2.parameters()[0] as EligibilityFunction
        assertThat(subFunction.rule()).isEqualTo(EligibilityRule.OR)
        assertThat(subFunction.parameters()).hasSize(2)

        val cohortB = findCohort(trial.cohorts(), "B")
        assertThat(cohortB.metadata().description()).isEqualTo("Cohort B")
        assertThat(cohortB.eligibility()).isEmpty()

        val cohortC = findCohort(trial.cohorts(), "C")
        assertThat(cohortC.metadata().description()).isEqualTo("Cohort C")
        assertThat(cohortC.eligibility()).isEmpty()
    }

    companion object {
        private val TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").path

        private fun findTrial(trials: List<Trial>, trialId: String): Trial {
            return trials.firstOrNull { it.identification().trialId() == trialId }
                ?: throw IllegalStateException("Could not find trial with ID: $trialId")
        }

        private fun findCohort(cohorts: List<Cohort>, cohortId: String): Cohort {
            return cohorts.firstOrNull { it.metadata().cohortId() == cohortId }
                ?: throw IllegalStateException("Could not find cohort with ID: $cohortId")
        }

        private fun findFunction(eligibility: List<Eligibility>, rule: EligibilityRule): EligibilityFunction {
            return eligibility.firstOrNull { it.function().rule() == rule }?.function()
                ?: throw IllegalStateException("Could not find eligibility function with rule: $rule")
        }
    }
}