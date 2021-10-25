package com.hartwig.actin.treatment.database.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public final class TrialDefinitionConfigFactory {

    private static final String DELIMITER = "\t";

    private TrialDefinitionConfigFactory() {
    }

    @NotNull
    public static List<TrialDefinitionConfig> read(@NotNull String trialTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(trialTsv).toPath());

        List<TrialDefinitionConfig> configs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = ResourceFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return configs;
    }

    @NotNull
    private static TrialDefinitionConfig fromParts(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableTrialDefinitionConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .acronym(parts[fields.get("acronym")])
                .title(parts[fields.get("title")])
                .build();
    }
}
