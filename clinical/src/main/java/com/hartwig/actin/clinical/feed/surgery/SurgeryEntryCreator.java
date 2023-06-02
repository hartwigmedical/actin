package com.hartwig.actin.clinical.feed.surgery;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class SurgeryEntryCreator implements FeedEntryCreator<SurgeryEntry> {

    private static final String BIOPSY_PROCEDURE_DISPLAY = "Procedurele sedatie analgesie ANE op OK";

    public SurgeryEntryCreator() {
    }

    @NotNull
    @Override
    public SurgeryEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableSurgeryEntry.builder()
                .subject(line.trimmed("subject"))
                .classDisplay(line.string("class_display"))
                .periodStart(line.date("period_start"))
                .periodEnd(line.date("period_end"))
                .codeCodingDisplayOriginal(line.string("code_coding_display_original"))
                .encounterStatus(line.string("encounter_status"))
                .procedureStatus(line.string("procedure_status"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return !line.string("code_coding_display_original").equals(BIOPSY_PROCEDURE_DISPLAY);
    }
}
