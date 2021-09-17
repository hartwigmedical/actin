package com.hartwig.actin.clinical.curation;

import org.jetbrains.annotations.NotNull;

public class ClinicalCuration {

    @NotNull
    private final CurationDatabase database;

    public ClinicalCuration(@NotNull final CurationDatabase database) {
        this.database = database;
    }
}
