package com.hartwig.actin.clinical.curation.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.util.TsvUtil;

import org.jetbrains.annotations.NotNull;

public final class LesionLocationConfigFile {

    private static final String DELIMITER = "\t";

    private LesionLocationConfigFile() {
    }

    @NotNull
    public static List<LesionLocationConfig> read(@NotNull String lesionLocationTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(lesionLocationTsv).toPath());

        List<LesionLocationConfig> configs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = TsvUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return configs;
    }

    @NotNull
    private static LesionLocationConfig fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableLesionLocationConfig.builder()
                .input(parts[fieldIndexMap.get("input")])
                .location(parts[fieldIndexMap.get("location")])
                .ignoreWhenOtherLesion(CurationUtil.parseBoolean(parts[fieldIndexMap.get("ignoreWhenOtherLesion")]))
                .build();
    }
}
