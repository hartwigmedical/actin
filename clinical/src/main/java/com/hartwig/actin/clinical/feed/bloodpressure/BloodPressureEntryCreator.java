package com.hartwig.actin.clinical.feed.bloodpressure;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class BloodPressureEntryCreator implements FeedEntryCreator<BloodPressureEntry> {

    private static final Logger LOGGER = LogManager.getLogger(BloodPressureEntryCreator.class);

    public BloodPressureEntryCreator() {
    }

    @NotNull
    @Override
    public BloodPressureEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableBloodPressureEntry.builder()
                .subject(line.string("subject"))
                .effectiveDateTime(line.date("effectiveDateTime"))
                .codeCodeOriginal(line.string("code_code_original"))
                .codeDisplayOriginal(line.string("code_display_original"))
                .issued(line.optionalDate("issued"))
                .valueString(line.string("valueString"))
                .componentCodeCode(line.string("component_code_code"))
                .componentCodeDisplay(line.string("component_code_display"))
                .componentValueQuantityCode(line.string("component_valueQuantity_code"))
                .componentValueQuantityValue(line.number("component_valueQuantity_value"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        // In blood pressure data there can be entries with no value.
        // They likely should be filtered prior to being ingested in ACTIN.
        boolean valid = !line.string("component_valueQuantity_value").isEmpty();
        if (!valid) {
            LOGGER.warn("Invalid blood pressure line detected with component code display '{}'", line.string("component_code_display"));
        }
        return valid;
    }
}
