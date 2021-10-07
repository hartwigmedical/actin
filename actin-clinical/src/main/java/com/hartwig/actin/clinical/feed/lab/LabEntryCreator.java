package com.hartwig.actin.clinical.feed.lab;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class LabEntryCreator implements FeedEntryCreator<LabEntry> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    public LabEntryCreator() {
    }

    @NotNull
    @Override
    public LabEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableLabEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .codeCodeOriginal(parts[fieldIndexMap.get("code_code_original")])
                .codeDisplayOriginal(parts[fieldIndexMap.get("code_display_original")])
                .issued(FeedUtil.parseDate(parts[fieldIndexMap.get("issued")], FORMAT))
                .valueQuantityComparator(parts[fieldIndexMap.get("valueQuantity_comparator")])
                .valueQuantityValue(FeedUtil.parseDouble(parts[fieldIndexMap.get("valueQuantity_value")]))
                .valueQuantityUnit(parts[fieldIndexMap.get("valueQuantity_unit")])
                .interpretationDisplayOriginal(parts[fieldIndexMap.get("Interpretation_display_original")])
                .valueString(parts[fieldIndexMap.get("valueString")])
                .codeCode(parts[fieldIndexMap.get("code_code")])
                .referenceRangeText(parts[fieldIndexMap.get("referenceRange_text")])
                .build();
    }
}
