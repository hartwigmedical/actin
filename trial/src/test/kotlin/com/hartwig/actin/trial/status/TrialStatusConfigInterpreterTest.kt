package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.CTC_TRIAL_PREFIX
import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseEntryFactory
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusConfigInterpreterTest {

    private val trialStatusConfigInterpreter = TestTrialStatusConfigInterpreterFactory.createWithProperTestTrialStatusDatabase()

    @Test
    fun `Should not determine status when study is not trial status database study`() {
        val notIncludedStudy: TrialDefinitionConfig =
            TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = "not a trial status database study")
        assertThat(trialStatusConfigInterpreter.isTrialOpen(notIncludedStudy)).isNull()
    }

    @Test
    fun `Should trust trial status database study when inconsistent with trial config`() {
        val closedStudy: TrialDefinitionConfig = TestTrialDefinitionConfigFactory.MINIMAL.copy(
            trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
            open = false
        )

        // TEST_TRIAL_1 is assumed to be open in proper test trial status database
        assertThat(trialStatusConfigInterpreter.isTrialOpen(closedStudy)).isTrue
    }

    @Test
    fun `Should not determine status if study missing in trial status database`() {
        val nonExistingCTCStudy: TrialDefinitionConfig =
            TestTrialDefinitionConfigFactory.MINIMAL.copy(trialId = "$CTC_TRIAL_PREFIX non-existing")
        assertThat(trialStatusConfigInterpreter.isTrialOpen(nonExistingCTCStudy)).isNull()
    }

    @Test
    fun `Should trust trial status database cohort when inconsistent with cohort config`() {
        val closedCohort: CohortDefinitionConfig =
            TestCohortDefinitionConfigFactory.MINIMAL.copy(externalCohortIds = setOf("2"), open = false)

        // Cohort ID 2 is assumed to be open in proper test trial status database
        assertThat(trialStatusConfigInterpreter.resolveCohortMetadata(closedCohort).open).isTrue
    }

    @Test
    fun `Should fallback to cohort config when missing in trial status database`() {
        val openNotAvailable: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            externalCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = true,
            slotsAvailable = true
        )
        assertThat(trialStatusConfigInterpreter.resolveCohortMetadata(openNotAvailable).open).isTrue

        val closedNotAvailable: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            externalCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = false,
            slotsAvailable = false
        )
        assertThat(trialStatusConfigInterpreter.resolveCohortMetadata(closedNotAvailable).open).isFalse
    }

    @Test
    fun `Should assume closed without slots when missing entirely`() {
        val missing: CohortDefinitionConfig = TestCohortDefinitionConfigFactory.MINIMAL.copy(
            externalCohortIds = setOf(CohortStatusInterpreter.NOT_AVAILABLE),
            open = null,
            slotsAvailable = null
        )

        val metadata = trialStatusConfigInterpreter.resolveCohortMetadata(missing)
        assertThat(metadata.open).isFalse
        assertThat(metadata.slotsAvailable).isFalse
    }

    @Test
    fun `Should classify all studies as new when trial config is empty`() {
        // The proper trial status database has 3 trials: TEST_TRIAL_1, TEST_TRIAL_2 and IGNORE_TRIAL
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()

        val newStudyMETCs = trialStatusConfigInterpreter.extractNewTrialStatusDatabaseStudies(trialConfigs)
        assertThat(newStudyMETCs.map { it.metcStudyID }.toSet()).containsExactly(
            TestTrialData.TEST_TRIAL_METC_1,
            TestTrialData.TEST_TRIAL_METC_2
        )
        trialStatusConfigInterpreter.checkModelForNewTrials(trialConfigs)
    }

    @Test
    fun `Should find no new study METCs when all trials are configured`() {
        // The proper trial status database has 3 trials: TEST_TRIAL_1, TEST_TRIAL_2 and IGNORE_TRIAL
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1
            ),
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_2
            )
        )

        assertThat(trialStatusConfigInterpreter.extractNewTrialStatusDatabaseStudies(trialConfigs)).isEmpty()

        trialStatusConfigInterpreter.checkModelForNewTrials(trialConfigs)
    }

    @Test
    fun `Should find no new cohortIds when all cohorts are configured`() {
        // The proper trial status database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("1")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("2")
            )
        )

        assertThat(trialStatusConfigInterpreter.extractNewTrialStatusDatabaseCohorts(cohortConfigs)).isEmpty()

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun `Should find no new cohorts when cohort config is empty`() {
        // The proper trial status database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = emptyList()

        val newCohorts = trialStatusConfigInterpreter.extractNewTrialStatusDatabaseCohorts(cohortConfigs)
        assertThat(newCohorts).isEmpty()

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun `Should classify all cohorts as new when cohorts are not used while trial exists`() {
        // The proper trial status database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("9999")
            )
        )

        val newCohorts = trialStatusConfigInterpreter.extractNewTrialStatusDatabaseCohorts(cohortConfigs)
        assertThat(newCohorts.map { it.cohortId }).containsExactly(1, 2)

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
    }

    @Test
    fun `Should assume parent cohort with all children referenced is not new`() {
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("2")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("3")
            )
        )

        val modelWithOneParentTwoChildren =
            TrialStatusConfigInterpreter(
                TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase()
                    .copy(
                        entries = listOf(
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                metcStudyID = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = 1,
                                cohortParentId = null
                            ),
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                metcStudyID = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = 2,
                                cohortParentId = 1
                            ),
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                metcStudyID = TestTrialData.TEST_TRIAL_METC_1,
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
    fun `Should find no unused MEC trial ids not in trial status database when all these trials are configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TestTrialData.TEST_MEC_NOT_IN_TRIAL_STATUS_DATABASE
            )
        )

        assertThat(trialStatusConfigInterpreter.extractUnusedStudiesNotInTrialStatusDatabase(trialConfigs)).isEmpty()

        trialStatusConfigInterpreter.checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigs)
    }

    @Test
    fun `Should find unused MEC trial ids not in trial status database when trial id not configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TestTrialData.TEST_TRIAL_METC_1
            )
        )

        assertThat(trialStatusConfigInterpreter.extractUnusedStudiesNotInTrialStatusDatabase(trialConfigs)).isEqualTo(
            listOf(
                TestTrialData.TEST_MEC_NOT_IN_TRIAL_STATUS_DATABASE
            )
        )

        trialStatusConfigInterpreter.checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigs)
    }

    @Test
    fun `Should not return validation errors when new trials if ignore is enabled `() {
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()
        val trialStatusConfigInterpreterIgnoringNewTrials =
            TestTrialStatusConfigInterpreterFactory.createWithProperTestTrialStatusDatabase(ignoreNewTrials = true)
        trialStatusConfigInterpreterIgnoringNewTrials.checkModelForNewTrials(trialConfigs)
        assertThat(trialStatusConfigInterpreterIgnoringNewTrials.validation().trialStatusDatabaseValidationErrors).isEmpty()
    }
}