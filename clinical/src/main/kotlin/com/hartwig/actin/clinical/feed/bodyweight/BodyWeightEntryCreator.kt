package com.hartwig.actin.clinical.feed.bodyweight

import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine

class BodyWeightEntryCreator : FeedEntryCreator<BodyWeightEntry> {
    override fun fromLine(line: FeedLine): BodyWeightEntry {
        return BodyWeightEntry(
            subject = line.trimmed("subject"),
            valueQuantityValue = line.number("valueQuantity_value"),
            valueQuantityUnit = line.string("valueQuantity_unit"),
            effectiveDateTime = line.bodyWeightDate("effectiveDateTime"),
            valid = true
        )
    }

    override fun isValid(line: FeedLine): Boolean {
        // A body weight of 0 can be assumed to be erroneous entry.
        return line.number("valueQuantity_value") > 0
    }
}