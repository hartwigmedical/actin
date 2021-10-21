package com.hartwig.actin.treatment.database.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public final class CohortConfigFile {

    private static final String DELIMITER = "\t";

    private CohortConfigFile() {
    }

    @NotNull
    public static List<CohortConfig> read(@NotNull String cohortTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(cohortTsv).toPath());

        List<CohortConfig> configs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = ResourceFile.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return configs;
    }

    @NotNull
    private static CohortConfig fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableCohortConfig.builder()
                .trialId(parts[fieldIndexMap.get("trialId")])
                .cohortId(parts[fieldIndexMap.get("cohortId")])
                .open(true)
                .description(parts[fieldIndexMap.get("description")])
                .build();
    }
}
