package com.hartwig.actin.clinical.feed.digitalfile

import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine

class DigitalFileEntryCreator : FeedEntryCreator<DigitalFileEntry> {
    override fun fromLine(line: FeedLine): DigitalFileEntry {
        return DigitalFileEntry(
            subject = line.trimmed("subject"),
            authored = line.date("authored"),
            description = line.string("description"),
            itemText = line.string("item_text"),
            itemAnswerValueValueString = line.string("item_answer_value_valueString")
        )
    }

    override fun isValid(line: FeedLine): Boolean {
        return true
    }
}