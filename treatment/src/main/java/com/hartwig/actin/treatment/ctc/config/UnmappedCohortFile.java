package com.hartwig.actin.treatment.ctc.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.util.ResourceFile;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

public final class UnmappedCohortFile {

    private static final String DELIMITER = "\t";

    @NotNull
    public static Set<Integer> read(@NotNull String tsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(DELIMITER));

        return lines.subList(1, lines.size())
                .stream()
                .map(line -> ResourceFile.integer(line.split(DELIMITER, -1)[fields.get("cohortId")]))
                .collect(Collectors.toSet());
    }
}
