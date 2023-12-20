package com.hartwig.actin.clinical.feed.vitalfunction

import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine
import org.apache.logging.log4j.LogManager

class VitalFunctionEntryCreator : FeedEntryCreator<VitalFunctionEntry> {
    override fun fromLine(line: FeedLine): VitalFunctionEntry {
        return VitalFunctionEntry(
            subject = line.trimmed("subject"),
            effectiveDateTime = line.vitalFunctionDate("effectiveDateTime"),
            codeDisplayOriginal = line.string("code_display_original"),
            componentCodeDisplay = line.string("component_code_display"),
            quantityUnit = line.string("quantity_unit"),
            quantityValue = line.number("value_quantity")
        )
    }

    override fun isValid(line: FeedLine): Boolean {
        // In vital function data there can be entries with no or NULL value.
        // They likely should be filtered prior to being ingested in ACTIN.
        val category = line.string("code_display_original")
        var validCategory = true
        if (category.isEmpty()) {
            validCategory = false
            LOGGER.warn("Empty vital function category detected.")
        } else if (VitalFunctionExtraction.toCategory(category) == null) {
            validCategory = false
            LOGGER.warn("Invalid vital function category detected: {}", category)
        }
        val value = line.string("value_quantity")
        var validValue = true
        if (value.isEmpty()) {
            validValue = false
            if (validCategory) {
                LOGGER.warn("Empty vital function value detected with category '{}'", category)
            }
        }
        return validValue && validCategory
    }

    companion object {
        private val LOGGER = LogManager.getLogger(VitalFunctionEntryCreator::class.java)
    }
}