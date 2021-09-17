package com.hartwig.actin.clinical.curation;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

public class CurationModel {

    @NotNull
    private final CurationDatabase database;

    @NotNull
    public static CurationModel fromCurationDirectory(@NotNull String clinicalCurationDirectory) throws IOException {
        return new CurationModel(CurationDatabaseReader.read(clinicalCurationDirectory));
    }

    private CurationModel(@NotNull final CurationDatabase database) {
        this.database = database;
    }
}
