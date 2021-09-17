package com.hartwig.actin.clinical.curation.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.util.FileUtil;

import org.jetbrains.annotations.NotNull;

public final class OncologicalHistoryFile {

    private static final String DELIMITER = "\t";

    private static final String IGNORE_STRING = "<ignore>";

    private OncologicalHistoryFile() {
    }

    @NotNull
    public static List<OncologicalHistory> read(@NotNull String oncologicalHistoryTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(oncologicalHistoryTsv).toPath());

        List<OncologicalHistory> oncologicalHistories = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = FileUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            String[] parts = line.split(DELIMITER);
            boolean ignore = parts[fieldIndexMap.get("name")].equals(IGNORE_STRING);

            oncologicalHistories.add(ImmutableOncologicalHistory.builder()
                    .input(parts[fieldIndexMap.get("input")])
                    .ignore(ignore)
                    .curatedObject(!ignore ? curateObject(fieldIndexMap, parts) : null)
                    .build());
        }

        return oncologicalHistories;
    }

    @NotNull
    private static Object curateObject(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        boolean isSecondPrimary = !parts[fieldIndexMap.get("isSecondPrimary")].isEmpty();
        if (isSecondPrimary) {
            return ImmutablePriorSecondPrimary.builder()
                    .tumorLocation(parts[fieldIndexMap.get("tumorLocation")])
                    .tumorSubLocation(parts[fieldIndexMap.get("tumorSubLocation")])
                    .tumorType(parts[fieldIndexMap.get("tumorType")])
                    .tumorSubType(parts[fieldIndexMap.get("tumorSubType")])
                    .doids(CurationUtil.toDOIDs(parts[fieldIndexMap.get("doids")]))
                    .build();
        } else {
            return null;
        }
    }
}
