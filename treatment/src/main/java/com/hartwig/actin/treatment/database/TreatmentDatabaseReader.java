package com.hartwig.actin.treatment.database;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.treatment.database.config.CohortConfig;
import com.hartwig.actin.treatment.database.config.CohortConfigFile;
import com.hartwig.actin.treatment.database.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.database.config.InclusionCriteriaConfigFile;
import com.hartwig.actin.treatment.database.config.TrialConfig;
import com.hartwig.actin.treatment.database.config.TrialConfigFile;
import com.hartwig.actin.util.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class TreatmentDatabaseReader {

    private static final Logger LOGGER = LogManager.getLogger(TreatmentDatabaseReader.class);

    private static final String TRIAL_TSV = "trial.tsv";
    private static final String COHORT_TSV = "cohort.tsv";
    private static final String INCLUSION_CRITERIA_TSV = "inclusion_criteria.tsv";

    private TreatmentDatabaseReader() {
    }

    @NotNull
    public static TreatmentDatabase read(@NotNull String treatmentDirectory) throws IOException {
        LOGGER.info("Reading treatment config from {}", treatmentDirectory);

        String basePath = Paths.forceTrailingFileSeparator(treatmentDirectory);

        return ImmutableTreatmentDatabase.builder()
                .trialConfigs(readTrialConfigs(basePath + TRIAL_TSV))
                .cohortConfigs(readCohortConfigs(basePath + COHORT_TSV))
                .inclusionCriteriaConfigs(readInclusionCriteriaConfigs(basePath + INCLUSION_CRITERIA_TSV))
                .build();
    }

    @NotNull
    private static List<TrialConfig> readTrialConfigs(@NotNull String trialTsv) {
        List<TrialConfig> configs = TrialConfigFile.read(trialTsv);
        LOGGER.info(" Read {} trial configs from {}", configs.size(), trialTsv);
        return configs;
    }

    @NotNull
    private static List<CohortConfig> readCohortConfigs(@NotNull String cohortTsv) throws IOException {
        List<CohortConfig> configs = CohortConfigFile.read(cohortTsv);
        LOGGER.info(" Read {} cohort configs from {}", configs.size(), cohortTsv);
        return configs;
    }

    @NotNull
    private static List<InclusionCriteriaConfig> readInclusionCriteriaConfigs(@NotNull String inclusionCriteriaTsv) {
        List<InclusionCriteriaConfig> configs = InclusionCriteriaConfigFile.read(inclusionCriteriaTsv);
        LOGGER.info(" Read {} inclusion criteria configs from {}", configs.size(), inclusionCriteriaTsv);
        return configs;
    }
}
