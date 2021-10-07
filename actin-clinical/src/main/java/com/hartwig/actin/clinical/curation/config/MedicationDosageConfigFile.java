package com.hartwig.actin.clinical.curation.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.util.FileUtil;

import org.jetbrains.annotations.NotNull;

public final class MedicationDosageConfigFile {

    private static final String DELIMITER = "\t";

    private MedicationDosageConfigFile() {
    }

    @NotNull
    public static List<MedicationDosageConfig> read(@NotNull String medicationDosageTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(medicationDosageTsv).toPath());

        List<MedicationDosageConfig> configs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = FileUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return configs;
    }

    @NotNull
    private static MedicationDosageConfig fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableMedicationDosageConfig.builder()
                .input(parts[fieldIndexMap.get("input")])
                .dosageMin(CurationUtil.parseOptionalDouble(parts[fieldIndexMap.get("dosageMin")]))
                .dosageMax(CurationUtil.parseOptionalDouble(parts[fieldIndexMap.get("dosageMax")]))
                .unit(parts[fieldIndexMap.get("unit")])
                .frequencyUnit(parts[fieldIndexMap.get("frequencyUnit")])
                .ifNeeded(CurationUtil.parseOptionalBoolean(parts[fieldIndexMap.get("ifNeeded")]))
                .build();
    }
}
