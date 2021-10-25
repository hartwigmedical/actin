package com.hartwig.actin.treatment.database;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.treatment.database.config.CohortConfig;
import com.hartwig.actin.treatment.database.config.CohortConfigFactory;
import com.hartwig.actin.treatment.database.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.database.config.InclusionCriteriaConfigFactory;
import com.hartwig.actin.treatment.database.config.TrialConfigFile;
import com.hartwig.actin.treatment.database.config.TrialDefinitionConfig;
import com.hartwig.actin.treatment.database.config.TrialDefinitionConfigFactory;
import com.hartwig.actin.util.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class TreatmentDatabaseReader {

    private static final Logger LOGGER = LogManager.getLogger(TreatmentDatabaseReader.class);

    private static final String TRIAL_DEFINITION_TSV = "trial_definition.tsv";
    private static final String COHORT_TSV = "cohort.tsv";
    private static final String INCLUSION_CRITERIA_TSV = "inclusion_criteria.tsv";

    private TreatmentDatabaseReader() {
    }

    @NotNull
    public static TreatmentDatabase read(@NotNull String treatmentDirectory) throws IOException {
        LOGGER.info("Reading treatment config from {}", treatmentDirectory);

        String basePath = Paths.forceTrailingFileSeparator(treatmentDirectory);

        return ImmutableTreatmentDatabase.builder().trialDefinitionConfigs(readTrialDefinitionConfigs(basePath + TRIAL_DEFINITION_TSV))
                .cohortConfigs(readCohortConfigs(basePath + COHORT_TSV))
                .inclusionCriteriaConfigs(readInclusionCriteriaConfigs(basePath + INCLUSION_CRITERIA_TSV))
                .build();
    }

    @NotNull
    private static List<TrialDefinitionConfig> readTrialDefinitionConfigs(@NotNull String tsv) throws IOException {
        List<TrialDefinitionConfig> configs = TrialConfigFile.read(tsv, new TrialDefinitionConfigFactory());
        LOGGER.info(" Read {} trial definition configs from {}", configs.size(), tsv);
        return configs;
    }

    @NotNull
    private static List<CohortConfig> readCohortConfigs(@NotNull String tsv) throws IOException {
        List<CohortConfig> configs = TrialConfigFile.read(tsv, new CohortConfigFactory());
        LOGGER.info(" Read {} cohort configs from {}", configs.size(), tsv);
        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaConfig> readInclusionCriteriaConfigs(@NotNull String tsv) throws IOException {
        List<InclusionCriteriaConfig> configs = TrialConfigFile.read(tsv, new InclusionCriteriaConfigFactory());
        LOGGER.info(" Read {} inclusion criteria configs from {}", configs.size(), tsv);
        return configs;
    }
}
