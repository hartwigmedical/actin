package com.hartwig.actin.clinical.feed.lab;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class LabEntryCreator implements FeedEntryCreator<LabEntry> {

    public LabEntryCreator() {
    }

    @NotNull
    @Override
    public LabEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableLabEntry.builder()
                .subject(line.trimmed("subject"))
                .codeCodeOriginal(line.string("code_code_original"))
                .codeDisplayOriginal(line.string("code_display_original"))
                .valueQuantityComparator(line.string("valueQuantity_comparator"))
                .valueQuantityValue(line.number("valueQuantity_value"))
                .valueQuantityUnit(line.string("valueQuantity_unit"))
                .referenceRangeText(line.string("referenceRange_text"))
                .effectiveDateTime(line.date("effectiveDateTime"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return true;
    }
}
