package com.hartwig.actin.clinical.feed;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

public interface FeedEntryCreator<T extends FeedEntry> {

    @NotNull
    T fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts);
}
