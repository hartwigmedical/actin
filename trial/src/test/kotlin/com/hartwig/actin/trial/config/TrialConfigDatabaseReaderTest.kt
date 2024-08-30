package com.hartwig.actin.trial.config

import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialConfigDatabaseReaderTest {

    private val trialConfigDirectory = resourceOnClasspath("trial_config")

    @Test
    fun canReadFromTestDirectory() {
        val database = TrialConfigDatabaseReader.read(trialConfigDirectory)
        assertTrialDefinitionConfigs(database.trialDefinitionConfigs)
        assertCohortConfigs(database.cohortDefinitionConfigs)
        assertInclusionCriteriaConfigs(database.inclusionCriteriaConfigs)
        assertInclusionCriteriaReferenceConfigs(database.inclusionCriteriaReferenceConfigs)
        assertUnusedRulesToKeep(database.unusedRulesToKeep)
    }

    private fun assertTrialDefinitionConfigs(configs: List<TrialDefinitionConfig>) {
        assertThat(configs).hasSize(2)

        val config1 = findTrial(configs, "ACTN 2021")
        assertThat(config1.open).isTrue
        assertThat(config1.acronym).isEqualTo("ACTIN-1")
        assertThat(config1.title).isEqualTo("ACTIN is a study to evaluate a new treatment decision system in 2021")

        val config2 = findTrial(configs, "ACTN 2022")
        assertThat(config2.open).isNull()
        assertThat(config2.acronym).isEqualTo("ACTIN-2")
        assertThat(config2.title).isEqualTo("ACTIN is a study to evaluate a new treatment decision system in 2022")
    }

    private fun findTrial(configs: List<TrialDefinitionConfig>, trialId: String): TrialDefinitionConfig {
        return configs.firstOrNull { it.trialId == trialId }
            ?: throw IllegalStateException("Could not find trial definition config for ID: $trialId")
    }

    private fun assertCohortConfigs(configs: List<CohortDefinitionConfig>) {
        assertThat(configs).hasSize(2)

        val config1: CohortDefinitionConfig = findCohort(configs, "A")
        assertThat(config1.trialId).isEqualTo("ACTN 2021")
        assertThat(config1.externalCohortIds).containsExactlyInAnyOrder("1", "2")
        assertThat(config1.evaluable).isTrue
        assertThat(config1.open).isNull()
        assertThat(config1.slotsAvailable).isNull()
        assertThat(config1.blacklist).isFalse
        assertThat(config1.description).isEqualTo("Dose escalation phase (monotherapy)")

        val config2: CohortDefinitionConfig = findCohort(configs, "B")
        assertThat(config2.trialId).isEqualTo("ACTN 2021")
        assertThat(config2.externalCohortIds).containsExactly("wont_be_mapped_because_closed")
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

    private fun assertUnusedRulesToKeep(unusedRulesToKeep: List<String>) {
        val converted = unusedRulesToKeep.stream().map { EligibilityRule.valueOf(it) }
        assertThat(converted).isEqualTo(listOf(EligibilityRule.HAS_HLA_TYPE_X, EligibilityRule.HAS_POTENTIAL_HYPOKALEMIA))
    }

    private fun findReference(configs: List<InclusionCriteriaReferenceConfig>, referenceId: String): InclusionCriteriaReferenceConfig {
        return configs.firstOrNull { it.referenceId == referenceId }
            ?: throw IllegalStateException("Could not find reference config for ID $referenceId")
    }
}