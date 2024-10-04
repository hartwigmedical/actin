package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.CTC_TRIAL_PREFIX
import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.trial.config.TrialDefinitionConfig
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
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors).isNotEmpty
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
        trialStatusConfigInterpreter.checkModelForNewTrials(trialConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors.size).isEqualTo(2)
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

        trialStatusConfigInterpreter.checkModelForNewTrials(trialConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors).isEmpty()
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

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should find no new cohorts when cohort config is empty`() {
        // The proper trial status database has 3 cohorts: 1, 2 and (unmapped) 3
        val cohortConfigs: List<CohortDefinitionConfig> = emptyList()

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors).isEmpty()
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

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors.size).isEqualTo(2)
    }

    @Test
    fun `Should find no unused MEC trial ids not in trial status database when all these trials are configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TestTrialData.TEST_MEC_NOT_IN_TRIAL_STATUS_DATABASE
            )
        )

        trialStatusConfigInterpreter.checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors).isEmpty()
    }

    @Test
    fun `Should find unused MEC trial ids not in trial status database when trial id not configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TestTrialData.TEST_TRIAL_METC_1
            )
        )
        trialStatusConfigInterpreter.checkModelForUnusedStudiesNotInTrialStatusDatabase(trialConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors.size).isEqualTo(1)
    }

    @Test
    fun `Should not return validation errors when new trials if ignore is enabled `() {
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()
        val trialStatusConfigInterpreterIgnoringNewTrials =
            TestTrialStatusConfigInterpreterFactory.createWithProperTestTrialStatusDatabase(ignoreNewTrials = true)
        trialStatusConfigInterpreterIgnoringNewTrials.checkModelForNewTrials(trialConfigs)
        assertThat(trialStatusConfigInterpreterIgnoringNewTrials.validation().trialStatusDatabaseValidationErrors).isEmpty()
    }


    @Test
    fun `Should not return validation errors when no unused study METCs are defined`() {
        val trialStatusConfigInterpreter =
            TestTrialStatusConfigInterpreterFactory.createWithMinimalTestTrialStatusDatabase()
        trialStatusConfigInterpreter.checkModelForUnusedStudyMETCsToIgnore()
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors).isEmpty()
    }

    @Test
    fun `Should return validation errors as trial to be ignored is not on the trial status database`() {
        val properTrialStatusDatabase = TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase()
        val trialStatusDatabase =
            properTrialStatusDatabase.copy(entries = properTrialStatusDatabase.entries.filter { it.metcStudyID != "Ignore-Study" })
        val trialStatusConfigInterpreter = TrialStatusConfigInterpreter(
            trialStatusDatabase,
            CTC_TRIAL_PREFIX,
            true
        )
        trialStatusConfigInterpreter.checkModelForUnusedStudyMETCsToIgnore()
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors).isNotEmpty()
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors.size).isEqualTo(1)
    }

    @Test
    fun `Should not return validation errors when no unmapped cohort ids ids are defined`() {
        val trialStatusConfigInterpreter =
            TestTrialStatusConfigInterpreterFactory.createWithMinimalTestTrialStatusDatabase()
        trialStatusConfigInterpreter.checkModelForUnusedUnmappedCohortIds()
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors).isEmpty()
    }

    @Test
    fun `Should return validation errors as there are unmapped cohort ids not on the trial status database`() {
        val properTrialStatusDatabase = TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase()
        val trialStatusDatabase =
            properTrialStatusDatabase.copy(
                unmappedCohortIds = properTrialStatusDatabase.unmappedCohortIds + setOf("nonExistingId")
            )
        val trialStatusConfigInterpreter = TrialStatusConfigInterpreter(
            trialStatusDatabase,
            CTC_TRIAL_PREFIX,
            true
        )
        trialStatusConfigInterpreter.checkModelForUnusedUnmappedCohortIds()
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors).isNotEmpty()
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors.size).isEqualTo(1)
    }
}