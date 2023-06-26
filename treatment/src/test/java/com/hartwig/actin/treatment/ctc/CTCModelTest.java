package com.hartwig.actin.treatment.ctc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import com.google.common.io.Resources;
import com.hartwig.actin.treatment.TestTrialData;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.TestCohortDefinitionConfigFactory;
import com.hartwig.actin.treatment.trial.config.TestTrialDefinitionConfigFactory;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.junit.Test;

public class CTCModelTest {

    private static final String CTC_CONFIG_DIRECTORY = Resources.getResource("ctc_config").getPath();

    private final CTCModel model = TestCTCModelFactory.createWithProperTestCTCDatabase();

    @Test
    public void shouldNotCrashWhenCreatedFromTestResources() throws IOException {
        assertNotNull(CTCModel.createFromCTCConfigDirectory(CTC_CONFIG_DIRECTORY));
    }

    @Test
    public void shouldStickToTrialConfigWhenStudyIsNotCTCStudy() {
        TrialDefinitionConfig openRandomStudy = TestTrialDefinitionConfigFactory.builder().trialId("random 1").open(true).build();
        assertEquals(true, model.isTrialOpen(openRandomStudy));

        TrialDefinitionConfig closedRandomStudy = TestTrialDefinitionConfigFactory.builder().trialId("random 2").open(false).build();
        assertEquals(false, model.isTrialOpen(closedRandomStudy));
    }

    @Test
    public void shouldTrustCTCStudyWhenInconsistentWithTrialConfig() {
        TrialDefinitionConfig closedStudy = TestTrialDefinitionConfigFactory.builder()
                .trialId(CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_ID_1)
                .open(false)
                .build();

        // TEST_TRIAL_1 is assumed to be open in proper test CTC database
        assertEquals(true, model.isTrialOpen(closedStudy));
    }

    @Test
    public void shouldFallBackToTrialConfigIfStudyMissingInCTC() {
        TrialDefinitionConfig openCTCStudy =
                TestTrialDefinitionConfigFactory.builder().trialId(CTCModel.CTC_TRIAL_PREFIX + " random 1").open(true).build();
        assertEquals(true, model.isTrialOpen(openCTCStudy));

        TrialDefinitionConfig closedCTCStudy =
                TestTrialDefinitionConfigFactory.builder().trialId(CTCModel.CTC_TRIAL_PREFIX + " random 2").open(false).build();
        assertEquals(false, model.isTrialOpen(closedCTCStudy));
    }

    @Test
    public void shouldTrustCTCCohortWhenInconsistentWithCohortConfig() {
        CohortDefinitionConfig closedCohort = TestCohortDefinitionConfigFactory.builder().ctcCohortIds(Set.of("2")).open(false).build();

        // Cohort ID 2 is assumed to be open in proper test CTC database
        assertTrue(model.resolveCohortMetadata(closedCohort).open());
    }

    @Test
    public void shouldFallBackToCohortConfigWhenMissingInCTC() {
        CohortDefinitionConfig openNotAvailable = TestCohortDefinitionConfigFactory.builder()
                .ctcCohortIds(Set.of(CohortStatusInterpreter.NOT_AVAILABLE))
                .open(true)
                .slotsAvailable(true)
                .build();
        assertTrue(model.resolveCohortMetadata(openNotAvailable).open());

        CohortDefinitionConfig closedNotAvailable = TestCohortDefinitionConfigFactory.builder()
                .ctcCohortIds(Set.of(CohortStatusInterpreter.NOT_AVAILABLE))
                .open(false)
                .slotsAvailable(false)
                .build();
        assertFalse(model.resolveCohortMetadata(closedNotAvailable).open());
    }

    @Test
    public void shouldAssumeClosedWithoutSlotsWhenMissingEntirely() {
        CohortDefinitionConfig missing = TestCohortDefinitionConfigFactory.builder()
                .ctcCohortIds(Set.of(CohortStatusInterpreter.NOT_AVAILABLE))
                .open(null)
                .slotsAvailable(null)
                .build();

        CohortMetadata metadata = model.resolveCohortMetadata(missing);
        assertFalse(metadata.open());
        assertFalse(metadata.slotsAvailable());
    }
}