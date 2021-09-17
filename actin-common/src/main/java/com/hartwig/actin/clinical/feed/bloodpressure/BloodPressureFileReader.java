package com.hartwig.actin.clinical.feed.bloodpressure;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedFileReader;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class BloodPressureFileReader extends FeedFileReader<BloodPressureEntry> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    public BloodPressureFileReader() {
    }

    @NotNull
    @Override
    public BloodPressureEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableBloodPressureEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .effectiveDateTime(FeedUtil.parseDate(parts[fieldIndexMap.get("effectiveDateTime")], FORMAT))
                .codeCodeOriginal(parts[fieldIndexMap.get("code_code_original")])
                .codeDisplayOriginal(parts[fieldIndexMap.get("code_display_original")])
                .issued(FeedUtil.parseOptionalDate(parts[fieldIndexMap.get("issued")], FORMAT))
                .valueString(parts[fieldIndexMap.get("valueString")])
                .componentCodeCode(parts[fieldIndexMap.get("component_code_code")])
                .componentCodeDisplay(parts[fieldIndexMap.get("component_code_display")])
                .componentValueQuantityCode(parts[fieldIndexMap.get("component_valueQuantity_code")])
                .componentValueQuantityValue(Double.parseDouble(parts[fieldIndexMap.get("component_valueQuantity_value")]))
                .build();
    }
}
