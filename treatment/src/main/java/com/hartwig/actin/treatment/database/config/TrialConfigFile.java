package com.hartwig.actin.treatment.database.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public final class TrialConfigFile {

    private static final String DELIMITER = "\t";

    private TrialConfigFile() {
    }

    @NotNull
    public static List<TrialConfig> read(@NotNull String trialTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(trialTsv).toPath());

        List<TrialConfig> configs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = ResourceFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return configs;
    }

    @NotNull
    private static TrialConfig fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableTrialConfig.builder()
                .trialId(parts[fieldIndexMap.get("trialId")])
                .acronym(parts[fieldIndexMap.get("acronym")])
                .title(parts[fieldIndexMap.get("title")])
                .build();
    }
}
