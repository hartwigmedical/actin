package com.hartwig.actin.treatment.ctc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.ResourceFile;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

public final class CTCDatabaseEntryFile {

    private static final String DELIMITER = "\t";

    @NotNull
    public static List<CTCDatabaseEntry> read(@NotNull String tsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        List<CTCDatabaseEntry> entries = Lists.newArrayList();
        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            entries.add(create(fields, line.split(DELIMITER, -1)));
        }
        return entries;
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
