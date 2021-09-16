package com.hartwig.actin.clinical.feed.bloodpressure;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BloodPressureFile {

    private static final String DELIMITER = "\t";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-M-yyyy HH:mm");

    private BloodPressureFile() {
    }

    @NotNull
    public static List<BloodPressureEntry> read(@NotNull String bloodPressureTsv) throws IOException {
        List<String> lines = FeedUtil.readFeedFile(bloodPressureTsv);

        Map<String, Integer> fieldIndexMap = FeedUtil.createFieldIndexMap(lines.get(0), DELIMITER);
        List<BloodPressureEntry> entries = Lists.newArrayList();
        for (String line : lines.subList(1, lines.size())) {
            entries.add(fromLine(fieldIndexMap, line));
        }

        return entries;
    }

    @NotNull
    private static BloodPressureEntry fromLine(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String line) {
        String[] parts = FeedUtil.splitFeedLine(line, DELIMITER);

        return ImmutableBloodPressureEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .effectiveDateTime(parseDate(parts[fieldIndexMap.get("effectiveDateTime")]))
                .codeCodeOriginal(parts[fieldIndexMap.get("code_code_original")])
                .codeDisplayOriginal(parts[fieldIndexMap.get("code_display_original")])
                .issued(parseOptionalDate(parts[fieldIndexMap.get("issued")]))
                .valueString(parts[fieldIndexMap.get("valueString")])
                .componentCodeCode(parts[fieldIndexMap.get("component_code_code")])
                .componentCodeDisplay(parts[fieldIndexMap.get("component_code_display")])
                .componentValueQuantityCode(parts[fieldIndexMap.get("component_valueQuantity_code")])
                .componentValueQuantityValue(Double.parseDouble(parts[fieldIndexMap.get("component_valueQuantity_value")]))
                .build();
    }

    @Nullable
    private static LocalDate parseOptionalDate(@NotNull String date) {
        return !date.isEmpty() ? parseDate(date) : null;
    }

    @NotNull
    private static LocalDate parseDate(@NotNull String date) {
        return LocalDate.parse(date, FORMAT);
    }
}
