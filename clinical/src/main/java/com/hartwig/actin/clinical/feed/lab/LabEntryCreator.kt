package com.hartwig.actin.clinical.feed.lab

import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine

class LabEntryCreator : FeedEntryCreator<LabEntry> {
    override fun fromLine(line: FeedLine): LabEntry {
        return LabEntry(
            subject = line.trimmed("subject"),
            codeCodeOriginal = line.string("code_code_original"),
            codeDisplayOriginal = line.string("code_display_original"),
            valueQuantityComparator = line.string("valueQuantity_comparator"),
            valueQuantityValue = line.number("valueQuantity_value"),
            valueQuantityUnit = line.string("valueQuantity_unit"),
            referenceRangeText = line.string("referenceRange_text"),
            effectiveDateTime = line.date("effectiveDateTime")
        )
    }

    override fun isValid(line: FeedLine): Boolean {
        return true
    }
}