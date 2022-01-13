package com.hartwig.actin.clinical.feed.encounter;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class EncounterEntryCreator implements FeedEntryCreator<EncounterEntry> {

    public EncounterEntryCreator() {
    }

    @NotNull
    @Override
    public EncounterEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableEncounterEntry.builder().subject(line.trimmed("subject"))
                .type1Display(line.string("type1_display"))
                .classDisplay(line.string("class_display"))
                .periodStart(line.date("period_start"))
                .periodEnd(line.date("period_end"))
                .identifierValue(line.string("identifier_value"))
                .identifierSystem(line.string("identifier_system"))
                .codeCodingCodeOriginal(line.string("code_coding_code_original"))
                .codeCodingDisplayOriginal(line.string("code_coding_display_original"))
                .reason(line.string("reason"))
                .accessionValue(line.string("accession_value"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return true;
    }
}
