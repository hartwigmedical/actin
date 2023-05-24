package com.hartwig.actin.clinical.feed.questionnaire;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class QuestionnaireEntryCreator implements FeedEntryCreator<QuestionnaireEntry> {

    private static final String MANUAL_QUESTIONNAIRE_DESCRIPTION = "INT Consult";
    public static final String QUESTIONNAIRE_TEXT_FIELD = "item_answer_value_valueString";

    private final boolean invalidateManualQuestionnaires;
    private final QuestionnaireRawEntryMapper questionnaireRawEntryMapper;

    public QuestionnaireEntryCreator(final boolean invalidateManualQuestionnaires,
            final QuestionnaireRawEntryMapper questionnaireRawEntryMapper) {
        this.invalidateManualQuestionnaires = invalidateManualQuestionnaires;
        this.questionnaireRawEntryMapper = questionnaireRawEntryMapper;
    }

    @NotNull
    @Override
    public QuestionnaireEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableQuestionnaireEntry.builder()
                .subject(line.trimmed("subject"))
                .authored(line.date("authored"))
                .description(line.string("description"))
                .itemText(line.string("item_text"))
                .itemAnswerValueValueString(questionnaireRawEntryMapper.correctQuestionnaireEntry(line.string(line.hasColumn(
                        QUESTIONNAIRE_TEXT_FIELD) ? QUESTIONNAIRE_TEXT_FIELD : "text")))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return !invalidateManualQuestionnaires || !line.string("description").equals(MANUAL_QUESTIONNAIRE_DESCRIPTION);
    }
}
