package com.hartwig.actin.molecular.orange.curation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

public final class ExternalTreatmentMappingTsv {

    private static final String FIELD_DELIMITER = "\t";

    private ExternalTreatmentMappingTsv() {
    }

    @NotNull
    public static List<ExternalTreatmentMapping> read(@NotNull String tsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        List<ExternalTreatmentMapping> mappings = Lists.newArrayList();
        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(FIELD_DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            mappings.add(fromLine(line, fields));
        }
        return mappings;
    }

    @NotNull
    private static ExternalTreatmentMapping fromLine(@NotNull String line, @NotNull Map<String, Integer> fields) {
        String[] values = line.split(FIELD_DELIMITER, -1);
        return ImmutableExternalTreatmentMapping.builder()
                .externalTreatment(values[fields.get("externalTreatment")])
                .actinTreatment(values[fields.get("actinTreatment")])
                .build();
    }
}
