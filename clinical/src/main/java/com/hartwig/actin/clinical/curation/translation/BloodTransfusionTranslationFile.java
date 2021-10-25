package com.hartwig.actin.clinical.curation.translation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public final class BloodTransfusionTranslationFile {

    private static final String DELIMITER = "\t";

    private BloodTransfusionTranslationFile() {
    }

    @NotNull
    public static List<BloodTransfusionTranslation> read(@NotNull String bloodTransfusionTranslationTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(bloodTransfusionTranslationTsv).toPath());

        List<BloodTransfusionTranslation> translations = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = ResourceFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            translations.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return translations;
    }

    @NotNull
    private static BloodTransfusionTranslation fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableBloodTransfusionTranslation.builder()
                .product(parts[fieldIndexMap.get("product")])
                .translatedProduct(parts[fieldIndexMap.get("translatedProduct")])
                .build();
    }
}
