package com.hartwig.actin.clinical.datamodel;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TumorStage implements Displayable {
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

    @Override
    @NotNull
    public String display() {
        return this.toString();
    }
}
