package com.hartwig.actin.clinical.feed;

import org.jetbrains.annotations.NotNull;

public interface FeedEntryCreator<T extends FeedEntry> {

    @NotNull
    T fromLine(@NotNull FeedLine line);

    boolean isValid(@NotNull FeedLine line);
}
