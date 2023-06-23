package com.hartwig.actin.treatment.trial

import com.google.common.io.Resources
import com.hartwig.actin.treatment.trial.TrialConfigModel.Companion.create
import com.hartwig.actin.treatment.trial.config.TestTrialConfigFactory
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class TrialConfigModelTest {
    @Test
    @Throws(IOException::class)
    fun canCreateFromTrialConfigDirectory() {
        Assert.assertNotNull(create(TRIAL_CONFIG_DIRECTORY, TestEligibilityFactoryFactory.createTestEligibilityFactory()))
    }

    @Test
    fun canQueryMinimalModel() {
        val model = TrialConfigModel(TestTrialConfigFactory.createMinimalTestTrialConfigDatabase())
        Assert.assertTrue(model.trials().isEmpty())
        Assert.assertTrue(model.cohortsForTrial("any trial").isEmpty())
        Assert.assertTrue(model.generalInclusionCriteriaForTrial("any trial").isEmpty())
        Assert.assertTrue(model.specificInclusionCriteriaForCohort("any trial", "any cohort").isEmpty())
        Assert.assertTrue(model.referencesForTrial("any trial").isEmpty())
    }

    @Test
    fun canQueryProperModel() {
        val model = TrialConfigModel(TestTrialConfigFactory.createProperTestTrialConfigDatabase())
        Assert.assertEquals(2, model.trials().size.toLong())
        Assert.assertEquals(3, model.cohortsForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_1).size.toLong())
        Assert.assertEquals(1, model.generalInclusionCriteriaForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_1).size.toLong())
        Assert.assertEquals(2, model.specificInclusionCriteriaForCohort(TestTrialConfigFactory.TEST_TRIAL_ID_1, "A").size.toLong())
        Assert.assertEquals(0, model.specificInclusionCriteriaForCohort(TestTrialConfigFactory.TEST_TRIAL_ID_1, "B").size.toLong())
        Assert.assertEquals(3, model.referencesForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_1).size.toLong())
        Assert.assertEquals(0, model.cohortsForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_2).size.toLong())
        Assert.assertEquals(1, model.generalInclusionCriteriaForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_2).size.toLong())
        Assert.assertEquals(1, model.referencesForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_2).size.toLong())
    }

    companion object {
        private val TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").path
    }
}