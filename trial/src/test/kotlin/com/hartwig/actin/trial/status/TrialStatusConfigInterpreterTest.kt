package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusConfigInterpreterTest {

    private val trialStatusConfigInterpreter = TestTrialStatusConfigInterpreterFactory.createWithProperTestTrialStatusDatabase()

    @Test
    fun `Should not determine status when study is not trial status database study`() {
        val notIncludedStudy: TrialDefinitionConfig =
            TestTrialDefinitionConfigFactory.MINIMAL.copy(nctId = "not a trial status database study")
        assertThat(trialStatusConfigInterpreter.isTrialOpen(notIncludedStudy)).isNull()
    }

    @Test
    fun `Should trust trial status database study when inconsistent with trial config`() {
        val closedStudy: TrialDefinitionConfig = TestTrialDefinitionConfigFactory.MINIMAL.copy(
            nctId = TestTrialData.TEST_TRIAL_NCT_1,
            open = false
        )
        assertThat(trialStatusConfigInterpreter.isTrialOpen(closedStudy)).isTrue
        assertThat(trialStatusConfigInterpreter.validation().trialStatusConfigValidationErrors).isNotEmpty
    }

    @Test
    fun `Should trust trial status database cohort when inconsistent with cohort config`() {
        val closedCohort: CohortDefinitionConfig =
            TestCohortDefinitionConfigFactory.MINIMAL.copy(externalCohortIds = setOf("2"), open = false)
        
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
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()
        trialStatusConfigInterpreter.checkModelForNewTrials(trialConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors.size).isEqualTo(2)
    }

    @Test
    fun `Should find no new study METCs when all trials are configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                nctId = TestTrialData.TEST_TRIAL_NCT_1
            ),
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                nctId = TestTrialData.TEST_TRIAL_NCT_2
            )
        )

        trialStatusConfigInterpreter.checkModelForNewTrials(trialConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should find no new cohortIds when all cohorts are configured`() {
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                externalCohortIds = setOf("1")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                externalCohortIds = setOf("2")
            )
        )

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should find no new cohorts when cohort config is empty`() {
        val cohortConfigs: List<CohortDefinitionConfig> = emptyList()

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors).isEmpty()
    }

    @Test
    fun `Should classify all cohorts as new when cohorts are not used while trial exists`() {
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                externalCohortIds = setOf("9999")
            )
        )

        trialStatusConfigInterpreter.checkModelForNewCohorts(cohortConfigs)
        assertThat(trialStatusConfigInterpreter.validation().trialStatusDatabaseValidationErrors.size).isEqualTo(2)
    }
}