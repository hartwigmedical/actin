package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.feed.FeedEntryCreator
import com.hartwig.actin.clinical.feed.FeedLine

class QuestionnaireEntryCreator : FeedEntryCreator<QuestionnaireEntry> {
    override fun fromLine(line: FeedLine): QuestionnaireEntry {
        return QuestionnaireEntry(
            subject = line.trimmed("subject"),
            authored = line.date("authored"),
            description = line.string("description"),
            itemText = line.string("item_text"),
            text = line.string("text")
        )
    }

    override fun isValid(line: FeedLine): Boolean {
        return QUESTIONNAIRE_DESCRIPTIONS.contains(line.string("description"))
    }

    companion object {
        private val QUESTIONNAIRE_DESCRIPTIONS = setOf("INT Consult", "consultation")
    }
}