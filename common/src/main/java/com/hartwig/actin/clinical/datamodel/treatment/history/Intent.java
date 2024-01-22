package com.hartwig.actin.clinical.datamodel.treatment.history;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum Intent implements Displayable {
    ADJUVANT("Adjuvant"),
    NEOADJUVANT("Neoadjuvant"),
    INDUCTION("Induction"),
    CONSOLIDATION("Consolidation"),
    MAINTENANCE("Maintenance"),
    PALLIATIVE("Palliative");

    @NotNull
    private final String display;

    Intent(@NotNull final String display) {
        this.display = display;
    }

    @Override
    @NotNull
    public String display() {
        return display;
    }
}
