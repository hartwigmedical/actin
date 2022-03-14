package com.hartwig.actin.clinical.datamodel;

import com.hartwig.actin.clinical.interpretation.TumorTypeCategoryResolver;

import org.jetbrains.annotations.NotNull;

public enum TumorTypeCategory {
    CARCINOMA,
    ADENOCARCINOMA,
    SQUAMOUS_CELL_CARCINOMA,
    MELANOMA;

    @NotNull
    public String display() {
        return TumorTypeCategoryResolver.toString(this).toLowerCase();
    }
}
