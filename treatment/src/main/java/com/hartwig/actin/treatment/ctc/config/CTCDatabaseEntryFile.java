package com.hartwig.actin.treatment.ctc.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hartwig.actin.util.ResourceFile;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

public final class CTCDatabaseEntryFile {

    private static final String DELIMITER = "\t";

    @NotNull
    public static List<CTCDatabaseEntry> read(@NotNull String tsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(DELIMITER));

        return lines.subList(1, lines.size()).stream().map(line -> create(fields, line.split(DELIMITER, -1))).collect(Collectors.toList());
    }

    @NotNull
    private static CTCDatabaseEntry create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableCTCDatabaseEntry.builder()
                .studyId(ResourceFile.integer(parts[fields.get("StudyID")]))
                .studyMETC(parts[fields.get("StudyMETC")])
                .studyAcronym(parts[fields.get("StudyAcroniem")])
                .studyTitle(parts[fields.get("StudyTitle")])
                .studyStatus(parts[fields.get("StudyStatus")])
                .cohortId(ResourceFile.optionalInteger(parts[fields.get("CohortId")]))
                .cohortParentId(ResourceFile.optionalInteger(parts[fields.get("CohortParentId")]))
                .cohortName(ResourceFile.optionalString(parts[fields.get("CohortName")]))
                .cohortStatus(ResourceFile.optionalString(parts[fields.get("CohortStatus")]))
                .cohortSlotsNumberAvailable(ResourceFile.optionalInteger(parts[fields.get("CohortSlotsNumberAvailable")]))
                .cohortSlotsDateAvailable(ResourceFile.optionalString(parts[fields.get("CohortSlotsDateAvailable")]))
                .build();
    }
}
