package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.google.common.io.Resources;
import com.hartwig.actin.treatment.trial.config.TestTrialConfigFactory;

import org.junit.Test;

public class TrialConfigModelTest {

    private static final String TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").getPath();

    @Test
    public void canCreateFromTrialConfigDirectory() throws IOException {
        assertNotNull(TrialConfigModel.create(TRIAL_CONFIG_DIRECTORY, TestEligibilityFactoryFactory.createTestEligibilityFactory()));
    }

    @Test
    public void canQueryMinimalModel() {
        TrialConfigModel model = new TrialConfigModel(TestTrialConfigFactory.createMinimalTestTrialConfigDatabase());

        assertTrue(model.trials().isEmpty());
        assertTrue(model.cohortsForTrial("any trial").isEmpty());
        assertTrue(model.generalInclusionCriteriaForTrial("any trial").isEmpty());
        assertTrue(model.specificInclusionCriteriaForCohort("any trial", "any cohort").isEmpty());
        assertTrue(model.referencesForTrial("any trial").isEmpty());
    }

    @Test
    public void canQueryProperModel() {
        TrialConfigModel model = new TrialConfigModel(TestTrialConfigFactory.createProperTestTrialConfigDatabase());

        assertEquals(2, model.trials().size());

        assertEquals(3, model.cohortsForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_1).size());
        assertEquals(1, model.generalInclusionCriteriaForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_1).size());
        assertEquals(2, model.specificInclusionCriteriaForCohort(TestTrialConfigFactory.TEST_TRIAL_ID_1, "A").size());
        assertEquals(0, model.specificInclusionCriteriaForCohort(TestTrialConfigFactory.TEST_TRIAL_ID_1, "B").size());
        assertEquals(3, model.referencesForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_1).size());

        assertEquals(0, model.cohortsForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_2).size());
        assertEquals(1, model.generalInclusionCriteriaForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_2).size());
        assertEquals(1, model.referencesForTrial(TestTrialConfigFactory.TEST_TRIAL_ID_2).size());
    }
}