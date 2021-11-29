package com.hartwig.actin.serve;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;

public final class ServeRecordExtractor {

    private ServeRecordExtractor() {
    }

    @NotNull
    public static List<ServeRecord> extract(@NotNull List<Trial> trials) {
        List<ServeRecord> records = Lists.newArrayList();

        return records;
    }
}
