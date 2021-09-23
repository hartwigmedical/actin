package com.hartwig.actin.clinical.feed.questionnaire;

import org.jetbrains.annotations.NotNull;

enum QuestionnaireVersion {
    V0,
    V1_0A,
    V1_0B;

    @NotNull
    public static QuestionnaireVersion version(@NotNull QuestionnaireEntry questionnaire) {
        String[] lines = questionnaire.itemAnswerValueValueString().split("\n");

        for (String line : lines) {
            switch (line) {
                case "Relevant clinical history":
                    return V1_0B;
                case "Relevant patient history":
                    return V1_0A;
                case "Patient history":
                    return V0;
            }
        }

        throw new IllegalStateException("Could not resolve questionnaire version for " + questionnaire);
    }
}
