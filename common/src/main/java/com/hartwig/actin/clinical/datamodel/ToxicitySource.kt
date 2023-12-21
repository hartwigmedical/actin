package com.hartwig.actin.clinical.datamodel;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum ToxicitySource implements Displayable {
    QUESTIONNAIRE("Questionnaire"),
    EHR("EHR");

    @NotNull
    private final String display;

    ToxicitySource(@NotNull final String display) {
        this.display = display;
    }

    @Override
    @NotNull
    public String display() {
        return display;
    }
}
