package com.hartwig.actin.clinical.datamodel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TumorStage {
    I(null, "I"),
    II(null, "II"),
    IIA(TumorStage.II, "IIA"),
    IIB(TumorStage.II, "IIB"),
    III(null, "III"),
    IIIA(TumorStage.III, "IIIA"),
    IIIB(TumorStage.III, "IIIB"),
    IIIC(TumorStage.III, "IIIC"),
    IV(null, "IV");

    @Nullable
    private final TumorStage category;
    @NotNull
    private final String display;

    TumorStage(@Nullable final TumorStage category, @NotNull final String display) {
        this.category = category;
        this.display = display;
    }

    @Nullable
    public TumorStage category() {
        return category;
    }

    @NotNull
    public String display() {
        return display;
    }
}
