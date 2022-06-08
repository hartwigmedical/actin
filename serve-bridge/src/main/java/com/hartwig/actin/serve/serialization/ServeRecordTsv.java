package com.hartwig.actin.serve.serialization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ServeRecordTsv {

    private static final String FIELD_DELIMITER = "\t";

    private ServeRecordTsv() {
    }

    public static void write(@NotNull String outputTsv, @NotNull Set<ServeRecord> records) throws IOException {
        List<String> lines = Lists.newArrayList();
        lines.add(header());
        for (ServeRecord record : records) {
            lines.add(toLine(record));
        }
        Files.write(new File(outputTsv).toPath(), lines);
    }

    @NotNull
    private static String header() {
        return new StringJoiner(FIELD_DELIMITER).add("trial")
                .add("cohort")
                .add("rule")
                .add("gene")
                .add("mutation")
                .add("isUsedAsInclusion")
                .toString();
    }

    @NotNull
    private static String toLine(@NotNull ServeRecord record) {
        return new StringJoiner(FIELD_DELIMITER).add(record.trial())
                .add(nullToEmpty(record.cohort()))
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
}
