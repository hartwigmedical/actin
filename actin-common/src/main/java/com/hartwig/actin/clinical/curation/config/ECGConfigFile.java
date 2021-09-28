package com.hartwig.actin.clinical.curation.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.FileUtil;

import org.jetbrains.annotations.NotNull;

public final class ECGConfigFile {

    private static final String DELIMITER = "\t";

    private ECGConfigFile() {
    }

    @NotNull
    public static List<ECGConfig> read(@NotNull String ecgTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(ecgTsv).toPath());

        List<ECGConfig> ecgs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = FileUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            ecgs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return ecgs;
    }

    @NotNull
    private static ECGConfig fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableECGConfig.builder()
                .input(parts[fieldIndexMap.get("input")])
                .interpretation(parts[fieldIndexMap.get("interpretation")])
                .build();
    }
}
