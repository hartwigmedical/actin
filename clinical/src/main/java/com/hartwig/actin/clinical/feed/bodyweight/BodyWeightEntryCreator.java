package com.hartwig.actin.clinical.feed.bodyweight;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class BodyWeightEntryCreator implements FeedEntryCreator<BodyWeightEntry> {

    public BodyWeightEntryCreator() {
    }

    @NotNull
    @Override
    public BodyWeightEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableBodyWeightEntry.builder().subject(line.trimmed("subject"))
                .valueQuantityValue(line.number("valueQuantity_value"))
                .valueQuantityUnit(line.string("valueQuantity_unit"))
                .effectiveDateTime(line.date("effectiveDateTime"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        // A body weight of 0 can be assumed to be erroneous entry.
        return line.number("valueQuantity_value") > 0;
    }
}
