package com.hartwig.actin.treatment.ctc;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.hartwig.actin.util.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class CTCDatabaseReader {

    private static final Logger LOGGER = LogManager.getLogger(CTCDatabaseReader.class);

    private static final String CTC_DATABASE_TSV = "ctc_database.tsv";
    private static final String IGNORE_STUDIES_TSV = "ignore_studies.tsv";
    private static final String UNMAPPED_COHORTS_TSV = "unmapped_cohorts.tsv";

    @NotNull
    public static CTCDatabase read(@NotNull String ctcConfigDirectory) throws IOException {
        LOGGER.info("Reading CTC config from {}", ctcConfigDirectory);

        String basePath = Paths.forceTrailingFileSeparator(ctcConfigDirectory);

        return ImmutableCTCDatabase.builder()
                .entries(readCTCDatabaseEntries(basePath + CTC_DATABASE_TSV))
                .studyMETCsToIgnore(readIgnoreStudies(basePath + IGNORE_STUDIES_TSV))
                .unmappedCohortIds(readUnmappedCohorts(basePath + UNMAPPED_COHORTS_TSV))
                .build();
    }

    @NotNull
    private static List<CTCDatabaseEntry> readCTCDatabaseEntries(@NotNull String tsv) throws IOException {
        List<CTCDatabaseEntry> entries = CTCDatabaseEntryFile.read(tsv);
        LOGGER.info(" Read {} CTC database entries from {}", entries.size(), tsv);
        return entries;
    }

    @NotNull
    private static Set<String> readIgnoreStudies(@NotNull String tsv) throws IOException {
        Set<String> ignoreStudies = IgnoreStudiesFile.read(tsv);
        LOGGER.info(" Read {} study METCs to ignore from {}", ignoreStudies.size(), tsv);
        return ignoreStudies;
    }

    @NotNull
    private static Set<Integer> readUnmappedCohorts(@NotNull String tsv) throws IOException {
        Set<Integer> unmappedCohorts = UnmappedCohortFile.read(tsv);
        LOGGER.info(" Read {} unmapped cohorts from {}", unmappedCohorts.size(), tsv);
        return unmappedCohorts;
    }
}
