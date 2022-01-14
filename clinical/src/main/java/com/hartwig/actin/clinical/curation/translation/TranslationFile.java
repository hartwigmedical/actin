package com.hartwig.actin.clinical.curation.translation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.util.TabularFile;

import org.jetbrains.annotations.NotNull;

public final class TranslationFile {

    private static final String DELIMITER = "\t";

    private TranslationFile() {
    }

    @NotNull
    public static <T extends Translation> List<T> read(@NotNull String tsv, @NotNull TranslationFactory<T> factory) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        List<T> translations = Lists.newArrayList();
        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            translations.add(factory.create(fields, line.split(DELIMITER, -1)));
        }
        return translations;
    }
}
