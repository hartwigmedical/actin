package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import org.jetbrains.annotations.NotNull;

public enum TransfusionProduct {
    ERYTHROCYTE("Erythrocyte"),
    THROMBOCYTE("Thrombocyte");

    @NotNull
    private final String display;

    TransfusionProduct(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
