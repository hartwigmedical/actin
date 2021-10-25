package com.hartwig.actin.treatment.trial.config;

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
    public static <T extends TrialConfig> List<T> read(@NotNull String tsv, @NotNull TrialConfigFactory<T> factory) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        List<T> configs = Lists.newArrayList();
        Map<String, Integer> fields = ResourceFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(factory.create(fields, line.split(DELIMITER, -1)));
        }
        return configs;
    }
}
