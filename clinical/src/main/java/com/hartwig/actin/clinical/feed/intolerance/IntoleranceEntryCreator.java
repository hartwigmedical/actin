package com.hartwig.actin.clinical.feed.intolerance;

import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class IntoleranceEntryCreator implements FeedEntryCreator<IntoleranceEntry> {

    public IntoleranceEntryCreator() {
    }

    @NotNull
    @Override
    public IntoleranceEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableIntoleranceEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .assertedDate(FeedUtil.parseDate(parts[fieldIndexMap.get("assertedDate")]))
                .category(parts[fieldIndexMap.get("category")])
                .categoryAllergyCategoryCode(parts[fieldIndexMap.get("category_allergyCategory_code")])
                .categoryAllergyCategoryDisplay(parts[fieldIndexMap.get("category_allergyCategory_display")])
                .clinicalStatus(parts[fieldIndexMap.get("clinicalStatus")])
                .codeText(parts[fieldIndexMap.get("code_text")])
                .criticality(parts[fieldIndexMap.get("criticality")])
                .build();
    }
}
