package com.hartwig.actin.treatment.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.io.Resources;
import com.hartwig.actin.treatment.database.config.CohortConfig;
import com.hartwig.actin.treatment.database.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.database.config.TrialDefinitionConfig;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentDatabaseReaderTest {

    private static final String TREATMENT_DIRECTORY = Resources.getResource("treatment").getPath();

    @Test
    public void canReadFromTestDirectory() throws IOException {
        TreatmentDatabase database = TreatmentDatabaseReader.read(TREATMENT_DIRECTORY);

        assertTrialConfigs(database.trialConfigs());
        assertCohortConfigs(database.cohortConfigs());
        assertInclusionCriteriaConfigs(database.inclusionCriteriaConfigs());
    }

    private static void assertTrialConfigs(@NotNull List<TrialDefinitionConfig> configs) {
        assertEquals(1, configs.size());

        TrialDefinitionConfig config = configs.get(0);
        assertEquals("ACTN 2021", config.trialId());
        assertEquals("ACTIN", config.acronym());
        assertEquals("ACTIN is a study to evaluate a new treatment decision system.", config.title());
    }

    private static void assertCohortConfigs(@NotNull List<CohortConfig> configs) {
        assertEquals(2, configs.size());

        CohortConfig config1 = find(configs, "A");
        assertEquals("ACTN 2021", config1.trialId());
        assertTrue(config1.open());
        assertEquals("Dose escalation phase (monotherapy)", config1.description());

        CohortConfig config2 = find(configs, "B");
        assertEquals("ACTN 2021", config2.trialId());
        assertFalse(config2.open());
        assertEquals("Dose escalation phase (combination therapy)", config2.description());
    }

    @NotNull
    private static CohortConfig find(@NotNull List<CohortConfig> configs, @NotNull String cohortId) {
        for (CohortConfig config : configs) {
            if (config.cohortId().equals(cohortId)) {
                return config;
            }
        }

        throw new IllegalStateException("Could not find cohort config for ID: " + cohortId);
    }

    private static void assertInclusionCriteriaConfigs(@NotNull List<InclusionCriteriaConfig> configs) {
        assertEquals(1, configs.size());

        InclusionCriteriaConfig config = configs.get(0);
        assertEquals("ACTN 2021", config.trialId());
        assertTrue(config.appliesToCohorts().isEmpty());
        assertEquals(EligibilityRule.IS_ADULT, config.eligibilityRule());
        assertTrue(config.eligibilityParameters().isEmpty());
    }
}