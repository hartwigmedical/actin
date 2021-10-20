package com.hartwig.actin.clinical.curation.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.util.TsvUtil;

import org.jetbrains.annotations.NotNull;

public final class NonOncologicalHistoryConfigFile {

    private static final String DELIMITER = "\t";

    private NonOncologicalHistoryConfigFile() {
    }

    @NotNull
    public static List<NonOncologicalHistoryConfig> read(@NotNull String nonOncologicalHistoryTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(nonOncologicalHistoryTsv).toPath());

        List<NonOncologicalHistoryConfig> nonOncologicalHistories = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = TsvUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            String[] parts = line.split(DELIMITER, -1);
            boolean ignore = CurationUtil.ignore(parts[fieldIndexMap.get("name")]);

            nonOncologicalHistories.add(ImmutableNonOncologicalHistoryConfig.builder()
                    .input(parts[fieldIndexMap.get("input")])
                    .ignore(ignore)
                    .curated(!ignore ? curate(fieldIndexMap, parts) : null)
                    .build());
        }

        return nonOncologicalHistories;
    }

    @NotNull
    private static PriorOtherCondition curate(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutablePriorOtherCondition.builder()
                .name(parts[fieldIndexMap.get("name")])
                .doids(CurationUtil.parseDOID(parts[fieldIndexMap.get("doids")]))
                .category(parts[fieldIndexMap.get("category")])
                .build();
    }
}
