package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.status.config.TestCTCDatabaseFactory
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CTCModelTest {

    private val model = TestCTCModelFactory.createWithProperTestCTCDatabase()

    @Test
    fun `Should not determine status when study is not CTC study`() {
        val nonCTCStudy: TrialDefinitionConfig = TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = "not a CTC study")
        assertThat(model.isTrialOpen(nonCTCStudy)).isNull()
    }

    @Test
    fun `Should trust CTC study when inconsistent with trial config`() {
        val closedStudy: TrialDefinitionConfig = TestTrialDefinitionConfigFactory.MINIMAL.copy(
            trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
            open = false
        )

        // TEST_TRIAL_1 is assumed to be open in proper test CTC database
        assertThat(model.isTrialOpen(closedStudy)).isTrue
    }

    @Test
    fun `Should not determine status if study missing in CTC`() {
        val nonExistingCTCStudy: TrialDefinitionConfig =
            TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " non-existing")
        assertThat(model.isTrialOpen(nonExistingCTCStudy)).isNull()
    }

    @Test
    fun `Should trust CTC cohort when inconsistent with cohort config`() {
        val closedCohort: CohortDefinitionConfig =
            TestCohortDefinitionConfigFactory.MINIMAL.copy(externalCohortIds = setOf("2"), open = false)

        // Cohort ID 2 is assumed to be open in proper test CTC database
        assertThat(model.resolveCohortMetadata(closedCohort).open).isTrue
    }

    @Test
    fun `Should fallback to cohort config when missing in CTC`() {
        val openNotAvailable: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            externalCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = true,
            slotsAvailable = true
        )
        assertThat(model.resolveCohortMetadata(openNotAvailable).open).isTrue

        val closedNotAvailable: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            externalCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = false,
            slotsAvailable = false
        )
        assertThat(model.resolveCohortMetadata(closedNotAvailable).open).isFalse
    }

    @Test
    fun `Should assume closed without slots when missing entirely`() {
        val missing: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            externalCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = null,
            slotsAvailable = null
        )

        val metadata = model.resolveCohortMetadata(missing)
        assertThat(metadata.open).isFalse
        assertThat(metadata.slotsAvailable).isFalse
    }

    @Test
    fun `Should classify all studies as new when trial config is empty`() {
        // The proper CTC database has 3 trials: TEST_TRIAL_1, TEST_TRIAL_2 and IGNORE_TRIAL
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()

        val newStudyMETCs = model.extractNewTrialStatusDatabaseStudies(trialConfigs)
        assertThat(newStudyMETCs.map { it.studyMETC }.toSet()).containsExactly(
            TestTrialData.TEST_TRIAL_METC_1,
            TestTrialData.TEST_TRIAL_METC_2
        )
        model.checkModelForNewTrials(trialConfigs)
    }

    @Test
    fun `Should find no new study METCs when all trials are configured`() {
        // The proper CTC database has 3 trials: TEST_TRIAL_1, TEST_TRIAL_2 and IGNORE_TRIAL
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1
            ),
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_2
            )
        )

        assertThat(model.extractNewTrialStatusDatabaseStudies(trialConfigs)).isEmpty()

        model.checkModelForNewTrials(trialConfigs)
    }

    @Test
    fun `Should find no new cohortIds when all cohorts are configured`() {
        // The proper CTC database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("1")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("2")
            )
        )

        assertThat(model.extractNewTrialStatusDatabaseCohorts(cohortConfigs)).isEmpty()

        model.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun `Should find no new cohorts when cohort config is empty`() {
        // The proper CTC database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = emptyList()

        val newCohorts = model.extractNewTrialStatusDatabaseCohorts(cohortConfigs)
        assertThat(newCohorts).isEmpty()

        model.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun `Should classify all cohorts as new when cohorts are not used while trial exists`() {
        // The proper CTC database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("9999")
            )
        )

        val newCohorts = model.extractNewTrialStatusDatabaseCohorts(cohortConfigs)
        assertThat(newCohorts.map { it.cohortId }).containsExactly(1, 2)

        model.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun `Should assume parent cohort with all children referenced is not new`() {
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("2")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = TrialStatusConfigInterpreter.MEC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("3")
            )
        )

        val modelWithOneParentTwoChildren =
            TrialStatusConfigInterpreter(
                TestCTCDatabaseFactory.createMinimalTestCTCDatabase()
                    .copy(
                        entries = listOf(
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                studyMETC = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = 1,
                                cohortParentId = null
                            ),
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                studyMETC = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = 2,
                                cohortParentId = 1
                            ),
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                studyMETC = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = 3,
                                cohortParentId = 1
                            )
                        )
                    )
            )

        val newCohorts = modelWithOneParentTwoChildren.extractNewTrialStatusDatabaseCohorts(cohortConfigs)
        assertThat(newCohorts).isEmpty()
    }

    @Test
    fun `Should find no unused MEC trial ids not in CTC when all these trials are configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TestTrialData.TEST_MEC_NOT_IN_CTC
            )
        )

        assertThat(model.extractUnusedMECStudiesNotInTrialStatusDatabase(trialConfigs)).isEmpty()

        model.checkModelForUnusedMecStudiesNotInTrialStatusDatabase(trialConfigs)
    }

    @Test
    fun `Should find unused MEC trial ids not in CTC when trial id not configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TestTrialData.TEST_TRIAL_METC_1
            )
        )

        assertThat(model.extractUnusedMECStudiesNotInTrialStatusDatabase(trialConfigs)).isEqualTo(listOf(TestTrialData.TEST_MEC_NOT_IN_CTC))

        model.checkModelForUnusedMecStudiesNotInTrialStatusDatabase(trialConfigs)
    }

}