package com.hartwig.actin.clinical.feed.bloodpressure;

import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class BloodPressureEntryCreator implements FeedEntryCreator<BloodPressureEntry> {

    private static final Logger LOGGER = LogManager.getLogger(BloodPressureEntryCreator.class);

    public BloodPressureEntryCreator() {
    }

    @NotNull
    @Override
    public BloodPressureEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        // TODO: In latest blood pressure data, there are entries with no value. Sort out how to deal with them properly!
        String componentValueQuantityValue = parts[fieldIndexMap.get("component_valueQuantity_value")];
        double value = 0D;
        if (!componentValueQuantityValue.isEmpty()) {
            value = FeedUtil.parseDouble(componentValueQuantityValue);
        } else {
            LOGGER.warn("Empty value found for blood pressure with code '{}'", parts[fieldIndexMap.get("component_code_display")]);
        }

        return ImmutableBloodPressureEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .effectiveDateTime(FeedUtil.parseDate(parts[fieldIndexMap.get("effectiveDateTime")]))
                .codeCodeOriginal(parts[fieldIndexMap.get("code_code_original")])
                .codeDisplayOriginal(parts[fieldIndexMap.get("code_display_original")])
                .issued(FeedUtil.parseOptionalDate(parts[fieldIndexMap.get("issued")]))
                .valueString(parts[fieldIndexMap.get("valueString")])
                .componentCodeCode(parts[fieldIndexMap.get("component_code_code")])
                .componentCodeDisplay(parts[fieldIndexMap.get("component_code_display")])
                .componentValueQuantityCode(parts[fieldIndexMap.get("component_valueQuantity_code")])
                .componentValueQuantityValue(value)
                .build();
    }
}
