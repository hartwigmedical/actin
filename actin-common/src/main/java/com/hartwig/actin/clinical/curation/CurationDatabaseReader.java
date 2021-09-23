package com.hartwig.actin.clinical.curation;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfigFile;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfigFile;
import com.hartwig.actin.util.FileUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class CurationDatabaseReader {

    private static final Logger LOGGER = LogManager.getLogger(CurationDatabaseReader.class);

    private static final String PRIMARY_TUMOR_TSV = "primary_tumor.tsv";
    private static final String ONCOLOGICAL_HISTORY_TSV = "oncological_history.tsv";

    private CurationDatabaseReader() {
    }

    @NotNull
    public static CurationDatabase read(@NotNull String clinicalCurationDirectory) throws IOException {
        LOGGER.info("Reading clinical curation config from {}", clinicalCurationDirectory);

        String basePath = FileUtil.appendFileSeparator(clinicalCurationDirectory);

        return ImmutableCurationDatabase.builder()
                .primaryTumorConfigs(readPrimaryTumorConfigs(basePath + PRIMARY_TUMOR_TSV))
                .oncologicalHistoryConfigs(readOncologicalHistoryConfigs(basePath + ONCOLOGICAL_HISTORY_TSV))
                .build();
    }

    @NotNull
    private static List<PrimaryTumorConfig> readPrimaryTumorConfigs(@NotNull String primaryTumorTsv) throws IOException {
        List<PrimaryTumorConfig> configs = PrimaryTumorConfigFile.read(primaryTumorTsv);
        LOGGER.info(" Read {} primary tumor configs from {}", configs.size(), primaryTumorTsv);
        return configs;
    }

    @NotNull
    private static List<OncologicalHistoryConfig> readOncologicalHistoryConfigs(@NotNull String oncologicalHistoryTsv) throws IOException {
        List<OncologicalHistoryConfig> configs = OncologicalHistoryConfigFile.read(oncologicalHistoryTsv);
        LOGGER.info(" Read {} oncological history configs from {}", configs.size(), oncologicalHistoryTsv);
        return configs;
    }
}
