package com.hartwig.actin.clinical.curation;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

public class ClinicalCuration {

    @NotNull
    private final CurationDatabase database;

    @NotNull
    public static ClinicalCuration fromCurationDirectory(@NotNull String clinicalCurationDirectory) throws IOException {
        return new ClinicalCuration(CurationDatabaseReader.read(clinicalCurationDirectory));
    }

    private ClinicalCuration(@NotNull final CurationDatabase database) {
        this.database = database;
    }
}
