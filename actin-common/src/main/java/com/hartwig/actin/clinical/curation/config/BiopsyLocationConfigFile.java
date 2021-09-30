package com.hartwig.actin.clinical.curation.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.FileUtil;

import org.jetbrains.annotations.NotNull;

public final class BiopsyLocationConfigFile {

    private static final String DELIMITER = "\t";

    private BiopsyLocationConfigFile() {
    }

    @NotNull
    public static List<BiopsyLocationConfig> read(@NotNull String biopsyLocationTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(biopsyLocationTsv).toPath());

        List<BiopsyLocationConfig> configs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = FileUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return configs;
    }

    @NotNull
    private static BiopsyLocationConfig fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableBiopsyLocationConfig.builder()
                .input(parts[fieldIndexMap.get("input")])
                .location(parts[fieldIndexMap.get("location")])
                .build();
    }
}
