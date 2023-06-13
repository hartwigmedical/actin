package com.hartwig.actin.clinical.feed.digitalfile;

import java.time.LocalDate;

import com.hartwig.actin.clinical.feed.FeedEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class DigitalFileEntry implements FeedEntry {

    @NotNull
    private static final String BLOOD_TRANSFUSION_DESCRIPTION = "Aanvraag bloedproducten_test";
    private static final String TOXICITY_DESCRIPTION = "ONC Kuuroverzicht";

    @Override
    @NotNull
    public abstract String subject();

    @NotNull
    public abstract LocalDate authored();

    @NotNull
    public abstract String description();

    @NotNull
    public abstract String itemText();

    @NotNull
    public abstract String itemAnswerValueValueString();

    public boolean isBloodTransfusionEntry() {
        return description().equals(BLOOD_TRANSFUSION_DESCRIPTION);
    }

    public boolean isToxicityEntry() {
        return description().equals(TOXICITY_DESCRIPTION);
    }
}

