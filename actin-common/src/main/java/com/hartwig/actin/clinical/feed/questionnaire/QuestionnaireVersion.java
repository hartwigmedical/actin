package com.hartwig.actin.clinical.feed.questionnaire;

import org.jetbrains.annotations.NotNull;

enum QuestionnaireVersion {
    V0,
    V1;

    @NotNull
    public static QuestionnaireVersion version(@NotNull QuestionnaireEntry questionnaire) {
        String[] lines = questionnaire.itemAnswerValueValueString().split("\n");

        for (String line : lines) {
            if (line.equals("Patient history")) {
                return V0;
            } else if (line.equals("Relevant patient history") || line.equals("Relevant clinical history")) {
                // Within the scope of V1 the 'relevant patient history' section was renamed to 'relevant clinical history'
                return V1;
            }
        }

        throw new IllegalStateException("Could not resolve questionnaire version for " + questionnaire);
    }
}
