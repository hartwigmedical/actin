package com.hartwig.actin.clinical.feed.vitalfunction;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class VitalFunctionEntryCreator implements FeedEntryCreator<VitalFunctionEntry> {

    private static final Logger LOGGER = LogManager.getLogger(VitalFunctionEntryCreator.class);

    public VitalFunctionEntryCreator() {
    }

    @NotNull
    @Override
    public VitalFunctionEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableVitalFunctionEntry.builder()
                .subject(line.string("subject"))
                .effectiveDateTime(line.date("effectiveDateTime"))
                .codeCodeOriginal(line.string("code_code_original"))
                .codeDisplayOriginal(line.string("code_display_original"))
                .issued(line.optionalDate("issued"))
                .valueString(line.string("valueString"))
                .componentCodeCode(line.string("component_code_code"))
                .componentCodeDisplay(line.string("component_code_display"))
                .quantityUnit(line.string("quantity_unit"))
                .quantityValue(line.number("quantity_value"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        // In vital function data there can be entries with no or NULL value.
        // They likely should be filtered prior to being ingested in ACTIN.
        String value = line.string("quantity_value");
        boolean valid = !value.isEmpty() && !value.equals("NULL");
        if (!valid) {
            LOGGER.warn("Invalid vital function line detected with component code display '{}'", line.string("component_code_display"));
        }
        return valid;
    }
}
