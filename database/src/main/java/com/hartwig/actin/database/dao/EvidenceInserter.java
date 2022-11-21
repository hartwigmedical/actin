package com.hartwig.actin.database.dao;

import org.jetbrains.annotations.NotNull;
import org.jooq.InsertValuesStep3;
import org.jooq.Record;

class EvidenceInserter<T extends Record> {

    @NotNull
    private final InsertValuesStep3<T, Integer, String, String> inserter;

    public EvidenceInserter(@NotNull final InsertValuesStep3<T, Integer, String, String> inserter) {
        this.inserter = inserter;
    }

    public void write(int topicId, @NotNull String treatment, @NotNull String type) {
        //noinspection ResultOfMethodCallIgnored
        inserter.values(topicId, treatment, type);
    }

    public void execute() {
        inserter.execute();
    }
}
