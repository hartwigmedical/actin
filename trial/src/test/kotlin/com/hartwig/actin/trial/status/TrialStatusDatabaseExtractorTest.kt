package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.TestTrialData
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import com.hartwig.actin.trial.config.TestTrialDefinitionConfigFactory
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseEntryFactory
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusDatabaseExtractorTest {

    private val trialStatusDatabaseExtractor =
        TrialStatusDatabaseExtractor(TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase())

    @Test
    fun `Should return all studies as new when trial config is empty`() {
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()
        val newStudyMETCs = trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseStudies(trialConfigs)
        assertThat(newStudyMETCs.map { it.nctId }.toSet()).containsExactly(
            TestTrialData.TEST_TRIAL_NCT_1,
            TestTrialData.TEST_TRIAL_NCT_2
        )
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
        assertThat(trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseStudies(trialConfigs)).isEmpty()
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
        assertThat(trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseCohorts(cohortConfigs)).isEmpty()
    }

    @Test
    fun `Should find no new cohorts when cohort config is empty`() {
        val cohortConfigs: List<CohortDefinitionConfig> = emptyList()
        assertThat(trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseCohorts(cohortConfigs)).isEmpty()
    }

    @Test
    fun `Should classify all cohorts as new when cohorts are not used while trial exists`() {
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                externalCohortIds = setOf("9999")
            )
        )
        val newCohorts = trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseCohorts(cohortConfigs)
        assertThat(newCohorts.map { it.cohortId }).containsExactly("1", "2")
    }

    @Test
    fun `Should assume parent cohort with all children referenced is not new`() {
        val cohortConfigs: List<CohortDefinitionConfig> = listOf(
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                externalCohortIds = setOf("2")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                externalCohortIds = setOf("3")
            )
        )

        val minimalTrialStatusDatabaseExtractor =
            TrialStatusDatabaseExtractor(
                TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase()
                    .copy(
                        entries = listOf(
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                                cohortId = "1",
                                cohortParentId = null
                            ),
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                                cohortId = "2",
                                cohortParentId = "1"
                            ),
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                nctId = TestTrialData.TEST_TRIAL_NCT_1,
                                cohortId = "3",
                                cohortParentId = "1"
                            )
                        )
                    )
            )
        assertThat(minimalTrialStatusDatabaseExtractor.extractNewTrialStatusDatabaseCohorts(cohortConfigs)).isEmpty()
    }
}