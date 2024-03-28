package com.hartwig.actin.trial.config

import com.hartwig.actin.testutil.ResourceLocator
import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.interpretation.TestEligibilityFactoryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test

class TrialConfigModelTest {

    @Test
    fun `Should create from trial config directory`() {
        Assert.assertNotNull(TrialConfigModel.create(TRIAL_CONFIG_DIRECTORY, TestEligibilityFactoryFactory.createTestEligibilityFactory()))
    }

    @Test
    fun `Should query minimal model`() {
        val model = TrialConfigModel.createFromDatabase(
            TestTrialConfigDatabaseFactory.createMinimalTestTrialConfigDatabase(),
            TrialConfigDatabaseValidator(TestEligibilityFactoryFactory.createTestEligibilityFactory())
        )
        assertThat(model.trials()).isEmpty()
        assertThat(model.cohortsForTrial("any trial")).isEmpty()
        assertThat(model.generalInclusionCriteriaForTrial("any trial")).isEmpty()
        assertThat(model.specificInclusionCriteriaForCohort("any trial", "any cohort")).isEmpty()
        assertThat(model.referencesForTrial("any trial")).isEmpty()
    }

    @Test
    fun `Should query proper model`() {
        val model = TrialConfigModel.createFromDatabase(
            TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase(),
            TrialConfigDatabaseValidator(TestEligibilityFactoryFactory.createTestEligibilityFactory())
        )
        assertThat(model.trials()).hasSize(2)
        assertThat(model.cohortsForTrial(TestTrialData.TEST_TRIAL_METC_1)).hasSize(3)
        assertThat(model.generalInclusionCriteriaForTrial(TestTrialData.TEST_TRIAL_METC_1)).hasSize(1)
        assertThat(model.specificInclusionCriteriaForCohort(TestTrialData.TEST_TRIAL_METC_1, "A")).hasSize(2)
        assertThat(model.specificInclusionCriteriaForCohort(TestTrialData.TEST_TRIAL_METC_1, "B")).hasSize(0)
        assertThat(model.referencesForTrial(TestTrialData.TEST_TRIAL_METC_1)).hasSize(3)
        assertThat(model.cohortsForTrial(TestTrialData.TEST_TRIAL_METC_2)).hasSize(0)
        assertThat(model.generalInclusionCriteriaForTrial(TestTrialData.TEST_TRIAL_METC_2)).hasSize(1)
        assertThat(model.referencesForTrial(TestTrialData.TEST_TRIAL_METC_2)).hasSize(1)
    }

    companion object {
        private val TRIAL_CONFIG_DIRECTORY = ResourceLocator().onClasspath("trial_config")
    }
}