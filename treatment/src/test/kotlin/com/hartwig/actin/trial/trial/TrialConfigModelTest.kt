package com.hartwig.actin.trial.trial

import com.google.common.io.Resources
import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.trial.config.TestTrialConfigDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test

class TrialConfigModelTest {

    @Test
    fun canCreateFromTrialConfigDirectory() {
        Assert.assertNotNull(TrialConfigModel.create(TRIAL_CONFIG_DIRECTORY, TestEligibilityFactoryFactory.createTestEligibilityFactory()))
    }

    @Test
    fun canQueryMinimalModel() {
        val model = TrialConfigModel.createFromDatabase(TestTrialConfigDatabaseFactory.createMinimalTestTrialConfigDatabase())
        assertThat(model.trials()).isEmpty()
        assertThat(model.cohortsForTrial("any trial")).isEmpty()
        assertThat(model.generalInclusionCriteriaForTrial("any trial")).isEmpty()
        assertThat(model.specificInclusionCriteriaForCohort("any trial", "any cohort")).isEmpty()
        assertThat(model.referencesForTrial("any trial")).isEmpty()
    }

    @Test
    fun canQueryProperModel() {
        val model = TrialConfigModel.createFromDatabase(TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase())
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
        private val TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").path
    }
}