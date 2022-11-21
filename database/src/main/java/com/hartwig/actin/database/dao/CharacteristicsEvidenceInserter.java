package com.hartwig.actin.database.dao;

import org.jetbrains.annotations.NotNull;
import org.jooq.InsertValuesStep3;
import org.jooq.Record;

public class CharacteristicsEvidenceInserter<T extends Record> {

    @NotNull
    private final InsertValuesStep3<T, String, String, String> inserter;

    public CharacteristicsEvidenceInserter(@NotNull final InsertValuesStep3<T, String, String, String> inserter) {
        this.inserter = inserter;
    }

    public void write(@NotNull String sampleId, @NotNull String treatment, @NotNull String type) {
        //noinspection ResultOfMethodCallIgnored
        inserter.values(sampleId, treatment, type);
    }

    public void execute() {
        inserter.execute();
    }
}
