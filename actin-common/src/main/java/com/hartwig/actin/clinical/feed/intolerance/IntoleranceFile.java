package com.hartwig.actin.clinical.feed.intolerance;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public final class IntoleranceFile {

    private static final String DELIMITER = "\t";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    private IntoleranceFile() {
    }

    @NotNull
    public static List<IntoleranceEntry> read(@NotNull String intoleranceTsv) throws IOException {
        List<String> lines = FeedUtil.readFeedFile(intoleranceTsv);

        Map<String, Integer> fieldIndexMap = FeedUtil.createFieldIndexMap(lines.get(0), DELIMITER);
        List<IntoleranceEntry> entries = Lists.newArrayList();
        for (String line : lines.subList(1, lines.size())) {
            entries.add(fromLine(fieldIndexMap, line));
        }

        return entries;
    }

    @NotNull
    private static IntoleranceEntry fromLine(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String line) {
        String[] parts = FeedUtil.splitFeedLine(line, DELIMITER);

        return ImmutableIntoleranceEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .assertedDate(parseDate(parts[fieldIndexMap.get("assertedDate")]))
                .category(parts[fieldIndexMap.get("category")])
                .categoryAllergyCategoryCode(parts[fieldIndexMap.get("category_allergyCategory_code")])
                .categoryAllergyCategoryDisplay(parts[fieldIndexMap.get("category_allergyCategory_display")])
                .clinicalStatus(parts[fieldIndexMap.get("clinicalStatus")])
                .codeText(parts[fieldIndexMap.get("code_text")])
                .criticality(parts[fieldIndexMap.get("criticality")])
                .build();
    }

    @NotNull
    private static LocalDate parseDate(@NotNull String date) {
        return LocalDate.parse(date, FORMAT);
    }
}
