package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.Set;

import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;

public class QuestionnaireEntryCreator implements FeedEntryCreator<QuestionnaireEntry> {

    private static final Set<String> QUESTIONNAIRE_DESCRIPTIONS = Set.of("INT Consult", "consultation");

    @NotNull
    @Override
    public QuestionnaireEntry fromLine(@NotNull FeedLine line) {
        return ImmutableQuestionnaireEntry.builder()
                .subject(line.trimmed("subject"))
                .authored(line.date("authored"))
                .description(line.string("description"))
                .itemText(line.string("item_text"))
                .text(line.string("text"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull FeedLine line) {
        return QUESTIONNAIRE_DESCRIPTIONS.contains(line.string("description"));
    }
}
