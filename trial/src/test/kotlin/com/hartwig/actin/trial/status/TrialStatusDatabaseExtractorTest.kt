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

class TrialStatusDatabaseExtractorTest {

    private val trialStatusDatabaseExtractor = TrialStatusDatabaseExtractor(
        TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase(),
        CTC_TRIAL_PREFIX
    )

    @Test
    fun `Should return all studies as new when trial config is empty`() {
        val trialConfigs: List<TrialDefinitionConfig> = emptyList()
        val newStudyMETCs = trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseStudies(trialConfigs)
        assertThat(newStudyMETCs.map { it.metcStudyID }.toSet()).containsExactly(
            TestTrialData.TEST_TRIAL_METC_1,
            TestTrialData.TEST_TRIAL_METC_2
        )
    }

    @Test
    fun `Should find no new study METCs when all trials are configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1
            ),
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_2
            )
        )
        assertThat(trialStatusDatabaseExtractor.extractNewTrialStatusDatabaseStudies(trialConfigs)).isEmpty()
    }

    @Test
    fun `Should find no new cohortIds when all cohorts are configured`() {
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
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
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
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("2")
            ),
            TestCohortDefinitionConfigFactory.MINIMAL.copy(
                trialId = CTC_TRIAL_PREFIX + " " + TestTrialData.TEST_TRIAL_METC_1,
                externalCohortIds = setOf("3")
            )
        )

        val minimalTrialStatusDatabaseExtractor =
            TrialStatusDatabaseExtractor(
                TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase()
                    .copy(
                        entries = listOf(
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                metcStudyID = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = "1",
                                cohortParentId = null
                            ),
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                metcStudyID = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = "2",
                                cohortParentId = "1"
                            ),
                            TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(
                                metcStudyID = TestTrialData.TEST_TRIAL_METC_1,
                                cohortId = "3",
                                cohortParentId = "1"
                            )
                        )
                    ),
                CTC_TRIAL_PREFIX
            )
        assertThat(minimalTrialStatusDatabaseExtractor.extractNewTrialStatusDatabaseCohorts(cohortConfigs)).isEmpty()
    }

    @Test
    fun `Should find no unused MEC trial ids not in trial status database when all these trials are configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TestTrialData.TEST_MEC_NOT_IN_TRIAL_STATUS_DATABASE
            )
        )
        assertThat(trialStatusDatabaseExtractor.extractUnusedStudiesNotInTrialStatusDatabase(trialConfigs)).isEmpty()
    }

    @Test
    fun `Should find unused MEC trial ids not in trial status database when trial id not configured`() {
        val trialConfigs: List<TrialDefinitionConfig> = listOf(
            TestTrialDefinitionConfigFactory.MINIMAL.copy(
                trialId = TestTrialData.TEST_TRIAL_METC_1
            )
        )
        assertThat(trialStatusDatabaseExtractor.extractUnusedStudiesNotInTrialStatusDatabase(trialConfigs)).isEqualTo(
            listOf(
                TestTrialData.TEST_MEC_NOT_IN_TRIAL_STATUS_DATABASE
            )
        )
    }

    @Test
    fun `Should not return empty when no unused study METCs are defined`() {
        val minimalTrialStatusDatabaseExtractor = TrialStatusDatabaseExtractor(
            TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase(),
            CTC_TRIAL_PREFIX
        )
        assertThat(minimalTrialStatusDatabaseExtractor.extractUnusedStudyMETCsToIgnore()).isEmpty()
    }

    @Test
    fun `Should unused study as trial to be ignored is not on the trial status database`() {
        val properTrialStatusDatabase = TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase()
        val trialStatusDatabase =
            properTrialStatusDatabase.copy(entries = properTrialStatusDatabase.entries.filter { it.metcStudyID != "Ignore-Study" })
        val properTrialStatusDatabaseExtractor = TrialStatusDatabaseExtractor(
            trialStatusDatabase,
            CTC_TRIAL_PREFIX
        )
        assertThat(properTrialStatusDatabaseExtractor.extractUnusedStudyMETCsToIgnore()).isNotEmpty()
    }

    @Test
    fun `Should not return unused unmapped cohorts when no unmapped cohort ids ids are defined`() {
        val minimalTrialStatusDatabaseExtractor = TrialStatusDatabaseExtractor(
            TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase(),
            CTC_TRIAL_PREFIX
        )
        assertThat(minimalTrialStatusDatabaseExtractor.extractUnusedUnmappedCohorts()).isEmpty()
    }

    @Test
    fun `Should return unused unmapped cohorts as there are unmapped cohort ids not on the trial status database`() {
        val properTrialStatusDatabase = TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase()
        val trialStatusDatabase =
            properTrialStatusDatabase.copy(
                unmappedCohortIds = properTrialStatusDatabase.unmappedCohortIds + setOf("nonExistingId")
            )
        val properTrialStatusDatabaseExtractor = TrialStatusDatabaseExtractor(
            trialStatusDatabase,
            CTC_TRIAL_PREFIX
        )
        assertThat(properTrialStatusDatabaseExtractor.extractUnusedUnmappedCohorts()).isNotEmpty()
    }
}