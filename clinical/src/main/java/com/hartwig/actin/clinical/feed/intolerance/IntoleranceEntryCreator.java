package com.hartwig.actin.clinical.feed.intolerance;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class IntoleranceEntryCreator implements FeedEntryCreator<IntoleranceEntry> {

    public IntoleranceEntryCreator() {
    }

    @NotNull
    @Override
    public IntoleranceEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableIntoleranceEntry.builder()
                .subject(line.trimmed("subject"))
                .assertedDate(line.date("assertedDate"))
                .category(line.string("category"))
                .categoryAllergyCategoryDisplay(line.string("category_allergyCategory_display"))
                .clinicalStatus(line.string("clinicalStatus"))
                .verificationStatus(line.string("verificationStatus"))
                .codeText(line.string("code_text"))
                .criticality(line.string("criticality"))
                .isSideEffect(line.string("isSideEffect"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return true;
    }
}
