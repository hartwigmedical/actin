package com.hartwig.actin.clinical.feed.questionnaire;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class QuestionnaireEntryCreator implements FeedEntryCreator<QuestionnaireEntry> {

    public QuestionnaireEntryCreator() {
    }

    @NotNull
    @Override
    public QuestionnaireEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableQuestionnaireEntry.builder().subject(line.trimmed("subject"))
                .authored(line.date("authored"))
                .parentIdentifierValue(line.string("parent_identifier_value"))
                .questionnaireQuestionnaireValue(line.string("questionnaire_Questionnaire_value"))
                .description(line.string("description"))
                .itemText(line.string("item_text"))
                .itemAnswerValueValueString(line.string("item_answer_value_valueString"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return true;
    }
}
