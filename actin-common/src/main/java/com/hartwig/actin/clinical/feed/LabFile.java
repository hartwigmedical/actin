package com.hartwig.actin.clinical.feed;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

public final class LabFile {

    private static final String DELIMITER = "\t";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm");

    private LabFile() {
    }

    @NotNull
    public static List<LabEntry> read(@NotNull String labTsv) throws IOException {
        List<String> lines = FeedUtil.readFeedFile(labTsv);

        Map<String, Integer> fieldIndexMap = FeedUtil.createFieldIndexMap(lines.get(0), DELIMITER);
        List<LabEntry> entries = Lists.newArrayList();
        for (String line : lines.subList(1, lines.size())) {
            entries.add(fromLine(fieldIndexMap, line));
        }

        return entries;
    }

    @NotNull
    private static LabEntry fromLine(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String line) {
        String[] parts = FeedUtil.splitFeedLine(line, DELIMITER);

        return ImmutableLabEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .codeCodeOriginal(parts[fieldIndexMap.get("code_code_original")])
                .codeDisplayOriginal(parts[fieldIndexMap.get("code_display_original")])
                .issued(parseDate(parts[fieldIndexMap.get("issued")]))
                .valueQuantityComparator(parts[fieldIndexMap.get("valueQuantity_comparator")])
                .valueQuantityValue(parseDouble(parts[fieldIndexMap.get("valueQuantity_value")]))
                .valueQuantityUnit(parts[fieldIndexMap.get("valueQuantity_unit")])
                .interpretationDisplayOriginal(parts[fieldIndexMap.get("Interpretation_display_original")])
                .valueString(parts[fieldIndexMap.get("valueString")])
                .codeCode(parts[fieldIndexMap.get("code_code")])
                .referenceRangeText(parts[fieldIndexMap.get("referenceRange_text")])
                .build();
    }

    @NotNull
    private static LocalDate parseDate(@NotNull String date) {
        return LocalDate.parse(date, FORMAT);
    }

    private static double parseDouble(@NotNull String number) {
        String formatted = number.replace(",", ".");
        return Double.parseDouble(formatted);
    }
}
