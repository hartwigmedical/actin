package com.hartwig.actin.clinical.datamodel;

import org.jetbrains.annotations.NotNull;

public enum ToxicitySource {
    QUESTIONNAIRE("Questionnaire"),
    EHR("EHR");

    @NotNull
    private final String display;

    ToxicitySource(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
