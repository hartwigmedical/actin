package com.hartwig.actin.molecular.orange.curation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

public final class ExternalTrialMappingTsv {

    private static final String FIELD_DELIMITER = "\t";

    private ExternalTrialMappingTsv() {
    }

    @NotNull
    public static List<ExternalTrialMapping> read(@NotNull String tsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        List<ExternalTrialMapping> mappings = Lists.newArrayList();
        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(FIELD_DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            mappings.add(fromLine(line, fields));
        }
        return mappings;
    }

    @NotNull
    private static ExternalTrialMapping fromLine(@NotNull String line, @NotNull Map<String, Integer> fields) {
        String[] values = line.split(FIELD_DELIMITER, -1);
        return ImmutableExternalTrialMapping.builder()
                .externalTrial(values[fields.get("externalTrial")])
                .actinTrial(values[fields.get("actinTrial")])
                .build();
    }
}
