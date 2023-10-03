package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.ctc.config.TestCTCDatabaseEntryFactory
import com.hartwig.actin.trial.ctc.config.TestCTCDatabaseFactory
import com.hartwig.actin.trial.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.trial.trial.config.TrialDefinitionConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CTCModelTest {

    private val model = TestCTCModelFactory.createWithProperTestCTCDatabase()

    @Test
    fun shouldNotDetermineStatusWhenStudyIsNotCTCStudy() {
        val nonCTCStudy: TrialDefinitionConfig = TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = "not a CTC study")
        assertThat(model.isTrialOpen(nonCTCStudy)).isNull()
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
    fun shouldNotDetermineStatusIfStudyMissingInCTC() {
        val nonExistingCTCStudy: TrialDefinitionConfig =
            TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = CTCModel.CTC_TRIAL_PREFIX + " non-existing")
        assertThat(model.isTrialOpen(nonExistingCTCStudy)).isNull()
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

    @Test
    fun shouldClassifyAllStudyMETCsAsNewWhenTrialConfigIsEmpty() {
        // The proper CTC database has 3 trials: TEST_TRIAL_1, TEST_TRIAL_2 and IGNORE_TRIAL
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()

        val newStudyMETCs = model.extractNewCTCStudyMETCs(trialConfigs)
        assertThat(newStudyMETCs).containsExactly(TestTrialData.TEST_TRIAL_METC_1, TestTrialData.TEST_TRIAL_METC_2)

        model.checkModelForNewTrials(trialConfigs)
    }

    @Test
    fun shouldFindNoNewStudyMETCsWhenAllTrialsAreConfigured() {
        // The proper CTC database has 3 trials: TEST_TRIAL_1, TEST_TRIAL_2 and IGNORE_TRIAL
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1
            ),
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_2
            )
        )

        assertThat(model.extractNewCTCStudyMETCs(trialConfigs)).isEmpty()

        model.checkModelForNewTrials(trialConfigs)
    }

    @Test
    fun shouldFindNoNewCohortIdsWhenAllCohortsAreConfigured() {
        // The proper CTC database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                ctcCohortIds = setOf("1")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                ctcCohortIds = setOf("2")
            )
        )

        assertThat(model.extractNewCTCCohorts(cohortConfigs)).isEmpty()

        model.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun shouldFindNoNewCohortsWhenCohortConfigIsEmpty() {
        // The proper CTC database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = emptyList()

        val newCohorts = model.extractNewCTCCohorts(cohortConfigs)
        assertThat(newCohorts).isEmpty()

        model.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun shouldClassifyAllCohortsAsNewWhenCohortsAreNotUsedWhileTrialExists() {
        // The proper CTC database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                ctcCohortIds = setOf("9999")
            )
        )

        val newCohorts = model.extractNewCTCCohorts(cohortConfigs)
        assertThat(newCohorts.map { it.cohortId }).containsExactly(1, 2)

        model.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun shouldAssumeParentCohortWithAllChildrenReferencedIsNotNew() {
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                ctcCohortIds = setOf("2")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTCModel.CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                ctcCohortIds = setOf("3")
            )
        )

        val modelWithOneParentTwoChildren =
            CTCModel(
                TestCTCDatabaseFactory.createMinimalTestCTCDatabase()
                    .copy(
                        entries = listOf(
                            TestCTCDatabaseEntryFactory.MINIMAL.copy(
                                studyMETC = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = 1,
                                cohortParentId = null
                            ),
                            TestCTCDatabaseEntryFactory.MINIMAL.copy(
                                studyMETC = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = 2,
                                cohortParentId = 1
                            ),
                            TestCTCDatabaseEntryFactory.MINIMAL.copy(
                                studyMETC = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = 3,
                                cohortParentId = 1
                            )
                        )
                    )
            )

        val newCohorts = modelWithOneParentTwoChildren.extractNewCTCCohorts(cohortConfigs)
        assertThat(newCohorts).isEmpty()
    }
}