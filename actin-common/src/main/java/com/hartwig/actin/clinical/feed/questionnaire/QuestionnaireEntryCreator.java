package com.hartwig.actin.clinical.feed.questionnaire;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;

public class QuestionnaireEntryCreator implements FeedEntryCreator<QuestionnaireEntry> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public QuestionnaireEntryCreator() {
    }

    @NotNull
    @Override
    public QuestionnaireEntry fromParts(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        return ImmutableQuestionnaireEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .parentIdentifierValue(parts[fieldIndexMap.get("parent_identifier_value")])
                .authoredDateTime(FeedUtil.parseDate(transform(parts[fieldIndexMap.get("authored_datetime")]), FORMAT))
                .questionnaireQuestionnaireValue(parts[fieldIndexMap.get("questionnaire_Questionnaire_value")])
                .description(parts[fieldIndexMap.get("description")])
                .itemText(parts[fieldIndexMap.get("item_text")])
                .itemAnswerValueValueString(parts[fieldIndexMap.get("item_answer_value_valueString")])
                .build();
    }

    @NotNull
    private static String transform(@NotNull String date) {
        // This date contains microseconds so need to remove before parsing.
        return date.substring(0, date.length() - 4);
    }
}
