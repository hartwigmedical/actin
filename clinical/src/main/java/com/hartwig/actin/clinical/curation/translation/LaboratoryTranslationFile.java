package com.hartwig.actin.clinical.curation.translation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public final class LaboratoryTranslationFile {

    private static final String DELIMITER = "\t";

    private LaboratoryTranslationFile() {
    }

    @NotNull
    public static List<LaboratoryTranslation> read(@NotNull String laboratoryTranslationTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(laboratoryTranslationTsv).toPath());

        List<LaboratoryTranslation> translations = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = ResourceFile.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            translations.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return translations;
    }

    @NotNull
    private static LaboratoryTranslation fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableLaboratoryTranslation.builder()
                .code(parts[fieldIndexMap.get("code")])
                .translatedCode(parts[fieldIndexMap.get("translatedCode")])
                .name(parts[fieldIndexMap.get("name")])
                .translatedName(parts[fieldIndexMap.get("translatedName")])
                .build();
    }
}
