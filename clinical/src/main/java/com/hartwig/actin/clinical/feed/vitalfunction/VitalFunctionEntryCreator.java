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
        return ImmutableVitalFunctionEntry.builder().subject(line.trimmed("subject"))
                .effectiveDateTime(line.date("effectiveDateTime"))
                .codeCodeOriginal(line.string("code_code_original"))
                .codeDisplayOriginal(line.string("code_display_original"))
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
        String category = line.string("code_display_original");

        boolean validCategory = true;
        if (category.isEmpty()) {
            validCategory = false;
            LOGGER.warn("Empty vital function category detected.");
        } else if (VitalFunctionExtraction.toCategory(category) == null) {
            validCategory = false;
            LOGGER.warn("Invalid vital function category detected: {}", category);
        }

        String value = line.string("quantity_value");
        boolean validValue = true;
        if (value.isEmpty()) {
            validValue = false;
            if (validCategory) {
                LOGGER.warn("Empty vital function value detected with category '{}'", category);
            }
        }

        return validValue && validCategory;
    }
}
