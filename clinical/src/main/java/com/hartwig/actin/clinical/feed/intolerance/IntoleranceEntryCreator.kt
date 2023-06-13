package com.hartwig.actin.clinical.feed.intolerance

import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine

class IntoleranceEntryCreator : FeedEntryCreator<IntoleranceEntry> {
    override fun fromLine(line: FeedLine): IntoleranceEntry {
        return ImmutableIntoleranceEntry.builder()
            .subject(line.trimmed("subject"))
            .assertedDate(line.date("assertedDate"))
            .category(line.string("category"))
            .categoryAllergyCategoryDisplay(line.string("category_allergyCategory_display"))
            .clinicalStatus(line.string("clinicalStatus"))
            .verificationStatus(line.string("verificationStatus"))
            .codeText(line.string("code_text"))
            .criticality(line.string("criticality"))
            .isSideEffect(line.string("isSideEffect"))
            .build()
    }

    override fun isValid(line: FeedLine): Boolean {
        return true
    }
}