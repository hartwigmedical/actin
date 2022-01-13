package com.hartwig.actin.clinical.feed.complication;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class ComplicationEntryCreator implements FeedEntryCreator<ComplicationEntry> {

    public ComplicationEntryCreator() {
    }

    @NotNull
    @Override
    public ComplicationEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableComplicationEntry.builder().subject(line.trimmed("subject"))
                .identifierSystem(line.string("identifier_system"))
                .categoryCodeOriginal(line.string("category_code_original"))
                .categoryDisplay(line.string("category_display"))
                .categoryDisplayOriginal(line.string("category_display_original"))
                .clinicalStatus(line.string("clinicalStatus"))
                .codeCodeOriginal(line.string("code_code_original"))
                .codeDisplayOriginal(line.string("code_display_original"))
                .codeCode(line.string("code_code"))
                .codeDisplay(line.string("code_display"))
                .onsetPeriodStart(line.date("onsetPeriod_start"))
                .onsetPeriodEnd(line.optionalDate("onsetPeriod_end"))
                .severityCode(line.string("severity_code"))
                .severityDisplay(line.string("severity_display"))
                .severityDisplayNl(line.string("severity_display_nl"))
                .specialtyCodeOriginal(line.string("specialty_code_original"))
                .specialtyDisplayOriginal(line.string("specialty_display_original"))
                .verificationStatusCode(line.string("verificationStatus_code"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return true;
    }
}
