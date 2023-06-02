package com.hartwig.actin.clinical.feed.digitalfile;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class DigitalFileEntryCreator implements FeedEntryCreator<DigitalFileEntry> {

    @NotNull
    @Override
    public DigitalFileEntry fromLine(@NotNull FeedLine line) {
        return ImmutableDigitalFileEntry.builder()
                .subject(line.trimmed("subject"))
                .authored(line.date("authored"))
                .description(line.string("description"))
                .itemText(line.string("item_text"))
                .itemAnswerValueValueString(line.string("item_answer_value_valueString"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull FeedLine line) {
        return true;
    }
}
