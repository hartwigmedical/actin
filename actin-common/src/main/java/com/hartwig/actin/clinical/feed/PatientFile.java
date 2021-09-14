package com.hartwig.actin.clinical.feed;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.Sex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PatientFile {

    private static final Logger LOGGER = LogManager.getLogger(PatientFile.class);

    private static final String DELIMITER = "\t";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm");

    private PatientFile() {
    }

    @NotNull
    public static List<PatientEntry> read(@NotNull String patientTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(patientTsv).toPath(), StandardCharsets.UTF_16LE);

        // skip header
        List<PatientEntry> entries = Lists.newArrayList();
        for (String line : lines.subList(1, lines.size())) {
            entries.add(fromLine(line));
        }

        return entries;
    }

    @NotNull
    private static PatientEntry fromLine(@NotNull String line) {
        String[] parts = clean(line.split(DELIMITER));

        return ImmutablePatientEntry.builder()
                .id(parts[0])
                .subject(parts[1])
                .birthYear(Integer.parseInt(parts[2]))
                .sex(Sex.parseSex(parts[3]))
                .periodStart(parseDate(parts[4]))
                .periodEnd(parseOptionalDate(parts[5]))
                .build();
    }

    @NotNull
    private static String[] clean(@NotNull String[] inputs) {
        String[] cleaned = new String[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            if (input.startsWith("\"") && input.endsWith("\"")) {
                cleaned[i] = input.substring(1, input.length() - 1);
            } else {
                LOGGER.warn("No cleaning needed for '{}'!", input);
                cleaned[i] = input;
            }
        }

        return cleaned;
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
