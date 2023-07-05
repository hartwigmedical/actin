package com.hartwig.actin.treatment.ctc

import com.google.common.io.Resources
import com.hartwig.actin.treatment.TestTrialData
import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseFactory
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.treatment.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Test

class CTCModelTest {
    private val model = TestCTCModelFactory.createWithProperTestCTCDatabase()

    @Test
    fun shouldNotCrashWhenCreatedFromTestResources() {
        assertNotNull(CTCModel.createFromCTCConfigDirectory(CTC_CONFIG_DIRECTORY))
    }

    @Test
    fun shouldClassifyAllStudyMETCsAsNewWhenTrialConfigIsEmpty() {
        // The proper CTC database has 3 trials, TEST_TRIAL_1, TEST_TRIAL_2 and IGNORE_TRIAL
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()

        val newStudyMETCs = model.extractNewCTCStudyMETCs(trialConfigs)
        assertThat(newStudyMETCs.size).isEqualTo(2)
        assertThat(newStudyMETCs.contains(TestTrialData.TEST_TRIAL_METC_1)).isTrue
        assertThat(newStudyMETCs.contains(TestTrialData.TEST_TRIAL_METC_2)).isTrue
        assertThat(newStudyMETCs.contains(TestTrialData.TEST_TRIAL_METC_IGNORE)).isFalse

        model.checkDatabaseForNewTrials(trialConfigs)
    }

    @Test
    fun shouldFindNoNewStudyMETCsWhenAllTrialsAreConfigured() {
        // The proper CTC database has 3 trials, TEST_TRIAL_1, TEST_TRIAL_2 and IGNORE_TRIAL
        val trialConfigs: MutableList<TrialDefinitionConfig> = mutableListOf()
        trialConfigs.add(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1
            )
        )

        trialConfigs.add(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_2
            )
        )

        assertThat(model.extractNewCTCStudyMETCs(trialConfigs)).isEmpty()

        model.checkDatabaseForNewTrials(trialConfigs)
    }

    @Test
    fun shouldPickUpUnusedStudyMETCsToIgnore() {
        // The proper CTC database has no unused METCs to ignore
        assertThat(model.extractUnusedStudyMETCsToIgnore()).isEmpty()
        model.evaluateModelConfiguration()

        val modelWithUnused = CTCModel(TestCTCDatabaseFactory.createMinimalTestCTCDatabase().copy(studyMETCsToIgnore = setOf("unused")))
        val unusedStudyMETCs = modelWithUnused.extractUnusedStudyMETCsToIgnore()
        assertThat(unusedStudyMETCs.size).isEqualTo(1)
        assertThat(unusedStudyMETCs.contains("unused")).isTrue
        modelWithUnused.evaluateModelConfiguration()
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
            trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
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