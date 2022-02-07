package com.hartwig.actin.serve.serialization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ImmutableServeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.util.TabularFile;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ServeRecordTsv {

    private static final String FIELD_DELIMITER = "\t";

    private ServeRecordTsv() {
    }

    @NotNull
    public static List<ServeRecord> read(@NotNull String tsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(tsv).toPath());

        List<ServeRecord> records = Lists.newArrayList();
        Map<String, Integer> fields = TabularFile.createFields(lines.get(0).split(FIELD_DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            records.add(fromLine(line, fields));
        }
        return records;
    }

    public static void write(@NotNull String outputTsv, @NotNull List<ServeRecord> records) throws IOException {
        List<String> lines = Lists.newArrayList();
        lines.add(header());
        for (ServeRecord record : records) {
            lines.add(toLine(record));
        }
        Files.write(new File(outputTsv).toPath(), lines);
    }

    @NotNull
    private static String header() {
        return new StringJoiner(FIELD_DELIMITER).add("trial").add("rule").add("gene").add("mutation").add("isUsedAsInclusion").toString();
    }

    @NotNull
    private static ServeRecord fromLine(@NotNull String line, @NotNull Map<String, Integer> fields) {
        String[] values = line.split(FIELD_DELIMITER, -1);
        return ImmutableServeRecord.builder()
                .trial(values[fields.get("trial")])
                .rule(EligibilityRule.valueOf(values[fields.get("rule")]))
                .gene(emptyToNull(values[fields.get("gene")]))
                .mutation(emptyToNull(values[fields.get("mutation")]))
                .isUsedAsInclusion(Boolean.parseBoolean(values[fields.get("isUsedAsInclusion")]))
                .build();
    }

    @NotNull
    private static String toLine(@NotNull ServeRecord record) {
        return new StringJoiner(FIELD_DELIMITER).add(record.trial())
                .add(record.rule().toString())
                .add(nullToEmpty(record.gene()))
                .add(nullToEmpty(record.mutation()))
                .add(String.valueOf(record.isUsedAsInclusion()))
                .toString();
    }

    @NotNull
    private static String nullToEmpty(@Nullable String string) {
        return string != null ? string : Strings.EMPTY;
    }

    @Nullable
    private static String emptyToNull(@NotNull String string) {
        return !string.isEmpty() ? string : null;
    }
}
