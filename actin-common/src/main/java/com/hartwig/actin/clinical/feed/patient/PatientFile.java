package com.hartwig.actin.clinical.feed.patient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.Sex;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PatientFile {

    private static final String DELIMITER = "\t";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-M-yyyy HH:mm");

    private PatientFile() {
    }

    @NotNull
    public static List<PatientEntry> read(@NotNull String patientTsv) throws IOException {
        List<String> lines = FeedUtil.readFeedFile(patientTsv);

        Map<String, Integer> fieldIndexMap = FeedUtil.createFieldIndexMap(lines.get(0), DELIMITER);
        List<PatientEntry> entries = Lists.newArrayList();
        for (String line : lines.subList(1, lines.size())) {
            entries.add(fromLine(fieldIndexMap, line));
        }

        return entries;
    }

    @NotNull
    private static PatientEntry fromLine(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String line) {
        String[] parts = FeedUtil.splitFeedLine(line, DELIMITER);

        return ImmutablePatientEntry.builder()
                .id(parts[fieldIndexMap.get("ID")])
                .subject(parts[fieldIndexMap.get("subject")])
                .birthYear(Integer.parseInt(parts[fieldIndexMap.get("birth_year")]))
                .sex(Sex.parseSex(parts[fieldIndexMap.get("gender")]))
                .periodStart(parseDate(parts[fieldIndexMap.get("period_start")]))
                .periodEnd(parseOptionalDate(parts[fieldIndexMap.get("period_end")]))
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
