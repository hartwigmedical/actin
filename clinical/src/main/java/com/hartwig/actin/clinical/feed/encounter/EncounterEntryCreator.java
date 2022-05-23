package com.hartwig.actin.clinical.feed.encounter;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class EncounterEntryCreator implements FeedEntryCreator<EncounterEntry> {

    private static final String BIOPSY_PROCEDURE_DISPLAY = "Procedurele sedatie analgesie ANE op OK";

    public EncounterEntryCreator() {
    }

    @NotNull
    @Override
    public EncounterEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableEncounterEntry.builder()
                .subject(line.trimmed("subject"))
                .classDisplay(line.string("class_display"))
                .periodStart(line.date("period_start"))
                .periodEnd(line.date("period_end"))
                .codeCodingDisplayOriginal(line.string("code_coding_display_original"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return !line.string("code_coding_display_original").equals(BIOPSY_PROCEDURE_DISPLAY);
    }
}
