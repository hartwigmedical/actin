package com.hartwig.actin.database.dao;

import org.jetbrains.annotations.NotNull;
import org.jooq.InsertValuesStep4;
import org.jooq.Record;

class EvidenceInserter<T extends Record> {

    @NotNull
    private final InsertValuesStep4<T, Integer, String, String, String> inserter;

    public EvidenceInserter(@NotNull final InsertValuesStep4<T, Integer, String, String, String> inserter) {
        this.inserter = inserter;
    }

    public void write(int topicId, @NotNull String sampleId, @NotNull String treatment, @NotNull String type) {
        inserter.values(topicId, sampleId, treatment, type);
    }

    public void execute() {
        inserter.execute();
    }
}
