package com.hartwig.actin.treatment.ctc

import com.google.common.io.Resources
import com.hartwig.actin.treatment.TestTrialData
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.treatment.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class CTCModelTest {
    private val model = TestCTCModelFactory.createWithProperTestCTCDatabase()

    @Test
    @Throws(IOException::class)
    fun shouldNotCrashWhenCreatedFromTestResources() {
        Assert.assertNotNull(CTCModel.createFromCTCConfigDirectory(CTC_CONFIG_DIRECTORY))
    }

    @Test
    fun shouldStickToTrialConfigWhenStudyIsNotCTCStudy() {
        val openRandomStudy: TrialDefinitionConfig = TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = "random 1", open = true)
        assertThat(model.isTrialOpen(openRandomStudy)).isTrue
        val closedRandomStudy: TrialDefinitionConfig = TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = "random 2", open = false)
        assertThat(model.isTrialOpen(closedRandomStudy)).isFalse
    }

    @Test
    fun shouldTrustCTCStudyWhenInconsistentWithTrialConfig() {
        val closedStudy: TrialDefinitionConfig = TestTrialDefinitionConfigFactory.MINIMAL.copy(
            trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_ID_1,
            open = false
        )

        // TEST_TRIAL_1 is assumed to be open in proper test CTC database
        assertThat(model.isTrialOpen(closedStudy)).isTrue
    }

    @Test
    fun shouldFallBackToTrialConfigIfStudyMissingInCTC() {
        val openCTCStudy: TrialDefinitionConfig =
            TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = CTCModel.CTC_TRIAL_PREFIX + " random 1", open = true)
        assertThat(model.isTrialOpen(openCTCStudy)).isTrue
        val closedCTCStudy: TrialDefinitionConfig =
            TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = CTCModel.CTC_TRIAL_PREFIX + " random 2", open = false)
        assertThat(model.isTrialOpen(closedCTCStudy)).isFalse
    }

    @Test
    fun shouldTrustCTCCohortWhenInconsistentWithCohortConfig() {
        val closedCohort: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(ctcCohortIds = setOf("2"), open = false)

        // Cohort ID 2 is assumed to be open in proper test CTC database
        assertThat(model.resolveCohortMetadata(closedCohort).open()).isTrue
    }

    @Test
    fun shouldFallBackToCohortConfigWhenMissingInCTC() {
        val openNotAvailable: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            ctcCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = true,
            slotsAvailable = true
        )
        assertThat(model.resolveCohortMetadata(openNotAvailable).open()).isTrue
        val closedNotAvailable: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            ctcCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = false,
            slotsAvailable = false
        )
        assertThat(model.resolveCohortMetadata(closedNotAvailable).open()).isFalse
    }

    @Test
    fun shouldAssumeClosedWithoutSlotsWhenMissingEntirely() {
        val missing: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            ctcCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = null,
            slotsAvailable = null
        )
        val metadata = model.resolveCohortMetadata(missing)
        assertThat(metadata.open()).isFalse
        assertThat(metadata.slotsAvailable()).isFalse
    }

    companion object {
        private val CTC_CONFIG_DIRECTORY = Resources.getResource("ctc_config").path
    }
}