package com.hartwig.actin.clinical.feed.complication;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ComplicationFile {

    private static final String DELIMITER = "\t";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    private ComplicationFile() {
    }

    @NotNull
    public static List<ComplicationEntry> read(@NotNull String complicationTsv) throws IOException {
        List<String> lines = FeedUtil.readFeedFile(complicationTsv);

        Map<String, Integer> fieldIndexMap = FeedUtil.createFieldIndexMap(lines.get(0), DELIMITER);
        List<ComplicationEntry> entries = Lists.newArrayList();
        for (String line : lines.subList(1, lines.size())) {
            entries.add(fromLine(fieldIndexMap, line));
        }

        return entries;
    }

    @NotNull
    private static ComplicationEntry fromLine(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String line) {
        String[] parts = FeedUtil.splitFeedLine(line, DELIMITER);

        return ImmutableComplicationEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .identifierSystem(parts[fieldIndexMap.get("identifier_system")])
                .categoryCodeOriginal(parts[fieldIndexMap.get("category_code_original")])
                .categoryDisplay(parts[fieldIndexMap.get("category_display")])
                .categoryDisplayOriginal(parts[fieldIndexMap.get("category_display_original")])
                .clinicalStatus(parts[fieldIndexMap.get("clinicalStatus")])
                .codeCodeOriginal(parts[fieldIndexMap.get("code_code_original")])
                .codeDisplayOriginal(parts[fieldIndexMap.get("code_display_original")])
                .codeCode(parts[fieldIndexMap.get("code_code")])
                .codeDisplay(parts[fieldIndexMap.get("code_display")])
                .onsetPeriodStart(parseDate(parts[fieldIndexMap.get("onsetPeriod_start")]))
                .onsetPeriodEnd(parseOptionalDate(parts[fieldIndexMap.get("onsetPeriod_end")]))
                .severityCode(parts[fieldIndexMap.get("severity_code")])
                .severityDisplay(parts[fieldIndexMap.get("severity_display")])
                .severityDisplayNl(parts[fieldIndexMap.get("severity_display_nl")])
                .specialtyCodeOriginal(parts[fieldIndexMap.get("specialty_code_original")])
                .specialtyDisplayOriginal(parts[fieldIndexMap.get("specialty_display_original")])
                .verificationStatusCode(parts[fieldIndexMap.get("verificationStatus_code")])
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
