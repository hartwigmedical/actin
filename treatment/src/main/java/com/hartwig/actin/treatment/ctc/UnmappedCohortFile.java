package com.hartwig.actin.treatment.ctc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.util.ResourceFile;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

public final class UnmappedCohortFile {

    private static final String DELIMITER = "\t";

    @NotNull
    public static Set<Integer> read(@NotNull String tsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        Set<Integer> unmappedCohorts = Sets.newTreeSet();
        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            String[] parts = line.split(DELIMITER, -1);
            unmappedCohorts.add(ResourceFile.integer(parts[fields.get("cohortId")]));
        }
        return unmappedCohorts;
    }
}
