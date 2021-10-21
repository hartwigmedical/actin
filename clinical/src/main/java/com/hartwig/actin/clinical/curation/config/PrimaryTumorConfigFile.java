package com.hartwig.actin.clinical.curation.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public final class PrimaryTumorConfigFile {

    private static final String DELIMITER = "\t";

    private PrimaryTumorConfigFile() {
    }

    @NotNull
    public static List<PrimaryTumorConfig> read(@NotNull String primaryTumorTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(primaryTumorTsv).toPath());

        List<PrimaryTumorConfig> primaryTumors = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = ResourceFile.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            primaryTumors.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return primaryTumors;
    }

    @NotNull
    private static PrimaryTumorConfig fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutablePrimaryTumorConfig.builder()
                .input(parts[fieldIndexMap.get("input")])
                .primaryTumorLocation(parts[fieldIndexMap.get("primaryTumorLocation")])
                .primaryTumorSubLocation(parts[fieldIndexMap.get("primaryTumorSubLocation")])
                .primaryTumorType(parts[fieldIndexMap.get("primaryTumorType")])
                .primaryTumorSubType(parts[fieldIndexMap.get("primaryTumorSubType")])
                .primaryTumorExtraDetails(parts[fieldIndexMap.get("primaryTumorExtraDetails")])
                .doids(CurationUtil.toDOIDs(parts[fieldIndexMap.get("doids")]))
                .build();
    }
}
