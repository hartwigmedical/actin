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

public final class ToxicityConfigFile {

    private static final String DELIMITER = "\t";

    private ToxicityConfigFile() {
    }

    @NotNull
    public static List<ToxicityConfig> read(@NotNull String toxicityTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(toxicityTsv).toPath());

        List<ToxicityConfig> configs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = TsvUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return configs;
    }

    @NotNull
    private static ToxicityConfig fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        String name = parts[fieldIndexMap.get("name")];
        return ImmutableToxicityConfig.builder().input(parts[fieldIndexMap.get("input")]).ignore(CurationUtil.ignore(name)).name(name)
                .grade(CurationUtil.parseOptionalInteger(parts[fieldIndexMap.get("grade")]))
                .build();
    }
}
