package com.hartwig.actin.serve.serialization;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.jetbrains.annotations.NotNull;

public final class ServeRecordTsv {

    private static final String FIELD_DELIMITER = "\t";

    private static final String PARAM_DELIMITER = ", ";

    private ServeRecordTsv() {
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
        return new StringJoiner(FIELD_DELIMITER).add("trial").add("rule").add("parameters").toString();
    }

    @NotNull
    private static String toLine(@NotNull ServeRecord record) {
        StringJoiner params = new StringJoiner(PARAM_DELIMITER);
        for (String param : record.parameters()) {
            params.add(param);
        }

        return new StringJoiner(FIELD_DELIMITER).add(record.trial()).add(record.rule().toString()).add(params.toString()).toString();
    }
}
