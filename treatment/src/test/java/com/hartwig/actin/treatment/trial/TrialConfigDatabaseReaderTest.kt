package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialConfigDatabaseReaderTest {

    private static final String TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").getPath();

    @Test
    public void canReadFromTestDirectory() throws IOException {
        TrialConfigDatabase database = TrialConfigDatabaseReader.read(TRIAL_CONFIG_DIRECTORY);

        assertTrialDefinitionConfigs(database.trialDefinitionConfigs());
        assertCohortConfigs(database.cohortDefinitionConfigs());
        assertInclusionCriteriaConfigs(database.inclusionCriteriaConfigs());
        assertInclusionCriteriaReferenceConfigs(database.inclusionCriteriaReferenceConfigs());
    }

    private static void assertTrialDefinitionConfigs(@NotNull List<TrialDefinitionConfig> configs) {
        assertEquals(1, configs.size());

        TrialDefinitionConfig config = configs.get(0);
        assertEquals("ACTN 2021", config.trialId());
        assertEquals("ACTIN", config.acronym());
        assertEquals("ACTIN is a study to evaluate a new treatment decision system.", config.title());
    }

    private static void assertCohortConfigs(@NotNull List<CohortDefinitionConfig> configs) {
        assertEquals(2, configs.size());

        CohortDefinitionConfig config1 = findCohort(configs, "A");
        assertEquals("ACTN 2021", config1.trialId());
        assertTrue(config1.evaluable());
        assertTrue(config1.open());
        assertTrue(config1.slotsAvailable());
        assertFalse(config1.blacklist());
        assertEquals("Dose escalation phase (monotherapy)", config1.description());

        CohortDefinitionConfig config2 = findCohort(configs, "B");
        assertEquals("ACTN 2021", config2.trialId());
        assertFalse(config2.evaluable());
        assertFalse(config2.open());
        assertTrue(config2.slotsAvailable());
        assertFalse(config2.blacklist());
        assertEquals("Dose escalation phase (combination therapy)", config2.description());
    }

    @NotNull
    private static CohortDefinitionConfig findCohort(@NotNull List<CohortDefinitionConfig> configs, @NotNull String cohortId) {
        for (CohortDefinitionConfig config : configs) {
            if (config.cohortId().equals(cohortId)) {
                return config;
            }
        }

        throw new IllegalStateException("Could not find cohort definition config for ID: " + cohortId);
    }

    private static void assertInclusionCriteriaConfigs(@NotNull List<InclusionCriteriaConfig> configs) {
        assertEquals(1, configs.size());

        InclusionCriteriaConfig config = configs.get(0);
        assertEquals("ACTN 2021", config.trialId());
        assertTrue(config.appliesToCohorts().isEmpty());
        assertEquals("AND(IS_AT_LEAST_X_YEARS_OLD[18], HAS_METASTATIC_CANCER)", config.inclusionRule());
    }

    private static void assertInclusionCriteriaReferenceConfigs(@NotNull List<InclusionCriteriaReferenceConfig> configs) {
        assertEquals(2, configs.size());

        InclusionCriteriaReferenceConfig config1 = findReference(configs, "I-01");
        assertEquals("ACTN 2021", config1.trialId());
        assertEquals("Patient has to be 18 years old", config1.referenceText());

        InclusionCriteriaReferenceConfig config2 = findReference(configs, "I-02");
        assertEquals("ACTN 2021", config2.trialId());
        assertEquals("Patient has metastatic cancer", config2.referenceText());
    }

    @NotNull
    private static InclusionCriteriaReferenceConfig findReference(@NotNull List<InclusionCriteriaReferenceConfig> configs,
            @NotNull String referenceId) {
        for (InclusionCriteriaReferenceConfig config : configs) {
            if (config.referenceId().equals(referenceId)) {
                return config;
            }
        }

        throw new IllegalStateException("Could not find reference config for ID " + referenceId);
    }
}