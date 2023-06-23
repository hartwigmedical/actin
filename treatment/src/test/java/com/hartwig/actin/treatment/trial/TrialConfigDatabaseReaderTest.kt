package com.hartwig.actin.treatment.trial

import com.google.common.io.Resources
import com.hartwig.actin.treatment.trial.TrialConfigDatabaseReader.read
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class TrialConfigDatabaseReaderTest {
    @Test
    @Throws(IOException::class)
    fun canReadFromTestDirectory() {
        val database = read(TRIAL_CONFIG_DIRECTORY)
        assertTrialDefinitionConfigs(database.trialDefinitionConfigs())
        assertCohortConfigs(database.cohortDefinitionConfigs())
        assertInclusionCriteriaConfigs(database.inclusionCriteriaConfigs())
        assertInclusionCriteriaReferenceConfigs(database.inclusionCriteriaReferenceConfigs())
    }

    companion object {
        private val TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").path
        private fun assertTrialDefinitionConfigs(configs: List<TrialDefinitionConfig?>) {
            Assert.assertEquals(1, configs.size.toLong())
            val config = configs[0]
            Assert.assertEquals("ACTN 2021", config!!.trialId())
            Assert.assertEquals("ACTIN", config.acronym())
            Assert.assertEquals("ACTIN is a study to evaluate a new treatment decision system.", config.title())
        }

        private fun assertCohortConfigs(configs: List<CohortDefinitionConfig?>) {
            Assert.assertEquals(2, configs.size.toLong())
            val config1 = findCohort(configs, "A")
            Assert.assertEquals("ACTN 2021", config1.trialId())
            Assert.assertTrue(config1.evaluable())
            Assert.assertTrue(config1.open())
            Assert.assertTrue(config1.slotsAvailable())
            Assert.assertFalse(config1.blacklist())
            Assert.assertEquals("Dose escalation phase (monotherapy)", config1.description())
            val config2 = findCohort(configs, "B")
            Assert.assertEquals("ACTN 2021", config2.trialId())
            Assert.assertFalse(config2.evaluable())
            Assert.assertFalse(config2.open())
            Assert.assertTrue(config2.slotsAvailable())
            Assert.assertFalse(config2.blacklist())
            Assert.assertEquals("Dose escalation phase (combination therapy)", config2.description())
        }

        private fun findCohort(configs: List<CohortDefinitionConfig>, cohortId: String): CohortDefinitionConfig {
            for (config in configs) {
                if (config.cohortId() == cohortId) {
                    return config
                }
            }
            throw IllegalStateException("Could not find cohort definition config for ID: $cohortId")
        }

        private fun assertInclusionCriteriaConfigs(configs: List<InclusionCriteriaConfig?>) {
            Assert.assertEquals(1, configs.size.toLong())
            val config = configs[0]
            Assert.assertEquals("ACTN 2021", config!!.trialId())
            Assert.assertTrue(config.appliesToCohorts().isEmpty())
            Assert.assertEquals("AND(IS_AT_LEAST_X_YEARS_OLD[18], HAS_METASTATIC_CANCER)", config.inclusionRule())
        }

        private fun assertInclusionCriteriaReferenceConfigs(configs: List<InclusionCriteriaReferenceConfig?>) {
            Assert.assertEquals(2, configs.size.toLong())
            val config1 = findReference(configs, "I-01")
            Assert.assertEquals("ACTN 2021", config1.trialId())
            Assert.assertEquals("Patient has to be 18 years old", config1.referenceText())
            val config2 = findReference(configs, "I-02")
            Assert.assertEquals("ACTN 2021", config2.trialId())
            Assert.assertEquals("Patient has metastatic cancer", config2.referenceText())
        }

        private fun findReference(
            configs: List<InclusionCriteriaReferenceConfig>,
            referenceId: String
        ): InclusionCriteriaReferenceConfig {
            for (config in configs) {
                if (config.referenceId() == referenceId) {
                    return config
                }
            }
            throw IllegalStateException("Could not find reference config for ID $referenceId")
        }
    }
}