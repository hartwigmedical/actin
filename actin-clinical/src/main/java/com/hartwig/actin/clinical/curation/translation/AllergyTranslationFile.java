package com.hartwig.actin.clinical.curation.translation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.FileUtil;

import org.jetbrains.annotations.NotNull;

public final class AllergyTranslationFile {

    private static final String DELIMITER = "\t";

    private AllergyTranslationFile() {
    }

    @NotNull
    public static List<AllergyTranslation> read(@NotNull String allergyTranslationTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(allergyTranslationTsv).toPath());

        List<AllergyTranslation> translations = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = FileUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            translations.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return translations;
    }

    @NotNull
    private static AllergyTranslation fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableAllergyTranslation.builder()
                .name(parts[fieldIndexMap.get("name")])
                .translatedName(parts[fieldIndexMap.get("translatedName")])
                .build();
    }
}
