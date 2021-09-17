package com.hartwig.actin.clinical.feed.questionnaire;

import java.time.LocalDate;

import com.hartwig.actin.clinical.feed.FeedEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class QuestionnaireEntry implements FeedEntry {

    @NotNull
    public abstract String subject();

    @NotNull
    public abstract String parentIdentifierValue();

    @NotNull
    public abstract LocalDate authoredDateTime();

    @NotNull
    public abstract String questionnaireQuestionnaireValue();

    @NotNull
    public abstract String description();

    @NotNull
    public abstract String itemText();

    @NotNull
    public abstract String itemAnswerValueValueString();

}

