package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class QuestionnaireEntryCreator implements FeedEntryCreator<QuestionnaireEntry> {

    public QuestionnaireEntryCreator() {
    }

    @NotNull
    @Override
    public QuestionnaireEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableQuestionnaireEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .parentIdentifierValue(parts[fieldIndexMap.get("parent_identifier_value")])
                .authoredDateTime(FeedUtil.parseDate(parts[fieldIndexMap.get("authored_datetime")]))
                .questionnaireQuestionnaireValue(parts[fieldIndexMap.get("questionnaire_Questionnaire_value")])
                .description(parts[fieldIndexMap.get("description")])
                .itemText(parts[fieldIndexMap.get("item_text")])
                .itemAnswerValueValueString(parts[fieldIndexMap.get("item_answer_value_valueString")])
                .build();
    }
}
