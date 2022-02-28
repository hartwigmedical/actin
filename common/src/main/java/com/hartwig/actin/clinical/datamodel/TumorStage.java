package com.hartwig.actin.clinical.datamodel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TumorStage {
    I(null),
    II(null),
    IIA(TumorStage.II),
    IIB(TumorStage.II),
    III(null),
    IIIA(TumorStage.III),
    IIIB(TumorStage.III),
    IIIC(TumorStage.III),
    IV(null);

    @Nullable
    private final TumorStage category;

    TumorStage(@Nullable final TumorStage category) {
        this.category = category;
    }

    @Nullable
    public TumorStage category() {
        return category;
    }

    @NotNull
    public String display() {
        return this.toString();
    }
}
