package com.hartwig.actin.treatment.trial

import com.google.common.io.Resources
import com.hartwig.actin.treatment.trial.TrialConfigDatabaseReader.read
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.IOException

class TrialConfigDatabaseReaderTest {
    @Test
    @Throws(IOException::class)
    fun canReadFromTestDirectory() {
        val database = read(TRIAL_CONFIG_DIRECTORY)
        assertTrialDefinitionConfigs(database.trialDefinitionConfigs)
        assertCohortConfigs(database.cohortDefinitionConfigs)
        assertInclusionCriteriaConfigs(database.inclusionCriteriaConfigs)
        assertInclusionCriteriaReferenceConfigs(database.inclusionCriteriaReferenceConfigs)
    }

    companion object {
        private val TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").path

        private fun assertTrialDefinitionConfigs(configs: List<TrialDefinitionConfig>) {
            assertThat(configs).hasSize(1)

            val config = configs[0]
            assertThat(config.trialId).isEqualTo("ACTN 2021")
            assertThat(config.acronym).isEqualTo("ACTIN")
            assertThat(config.title).isEqualTo("ACTIN is a study to evaluate a new treatment decision system.")
        }

        private fun assertCohortConfigs(configs: List<CohortDefinitionConfig>) {
            assertThat(configs).hasSize(2)

            val config1: CohortDefinitionConfig = findCohort(configs, "A")
            assertThat(config1.trialId).isEqualTo("ACTN 2021")
            assertThat(config1.ctcCohortIds).containsExactlyInAnyOrder("1", "2")
            assertThat(config1.evaluable).isTrue
            assertThat(config1.open).isNull()
            assertThat(config1.slotsAvailable).isNull()
            assertThat(config1.blacklist).isFalse
            assertThat(config1.description).isEqualTo("Dose escalation phase (monotherapy)")

            val config2: CohortDefinitionConfig = findCohort(configs, "B")
            assertThat(config2.trialId).isEqualTo("ACTN 2021")
            assertThat(config2.ctcCohortIds).containsExactly("wont_be_mapped_because_closed")
            assertThat(config2.evaluable).isFalse
            assertThat(config2.open).isFalse
            assertThat(config2.slotsAvailable).isFalse
            assertThat(config2.blacklist).isTrue
            assertThat(config2.description).isEqualTo("Dose escalation phase (combination therapy)")
        }

        private fun findCohort(configs: List<CohortDefinitionConfig>, cohortId: String): CohortDefinitionConfig {
            return configs.firstOrNull { it.cohortId == cohortId }
                ?: throw IllegalStateException("Could not find cohort definition config for ID: $cohortId")
        }

        private fun assertInclusionCriteriaConfigs(configs: List<InclusionCriteriaConfig>) {
            assertThat(configs).hasSize(1)
            val config = configs[0]
            assertThat(config.trialId).isEqualTo("ACTN 2021")
            assertThat(config.appliesToCohorts).isEmpty()
            assertThat(config.inclusionRule).isEqualTo("AND(IS_AT_LEAST_X_YEARS_OLD[18], HAS_METASTATIC_CANCER)")
        }

        private fun assertInclusionCriteriaReferenceConfigs(configs: List<InclusionCriteriaReferenceConfig>) {
            assertThat(configs).hasSize(2)
            val config1: InclusionCriteriaReferenceConfig = findReference(configs, "I-01")
            assertThat(config1.trialId).isEqualTo("ACTN 2021")
            assertThat(config1.referenceText).isEqualTo("Patient has to be 18 years old")

            val config2: InclusionCriteriaReferenceConfig = findReference(configs, "I-02")
            assertThat(config2.trialId).isEqualTo("ACTN 2021")
            assertThat(config2.referenceText).isEqualTo("Patient has metastatic cancer")
        }

        private fun findReference(configs: List<InclusionCriteriaReferenceConfig>, referenceId: String): InclusionCriteriaReferenceConfig {
            return configs.firstOrNull { it.referenceId == referenceId }
                ?: throw IllegalStateException("Could not find reference config for ID $referenceId")
        }
    }
}