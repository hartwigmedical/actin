package com.hartwig.actin.clinical.feed.questionnaire;

import org.jetbrains.annotations.NotNull;

enum QuestionnaireVersion {
    V0_1,
    V1_0,
    V1_1;

    @NotNull
    public static QuestionnaireVersion version(@NotNull QuestionnaireEntry questionnaire) {
        String[] lines = questionnaire.itemAnswerValueValueString().split("\n");

        for (String line : lines) {
            switch (line) {
                case "Patient history":
                    return V0_1;
                case "Relevant clinical history":
                    return V1_0;
                case "Relevant patient history":
                    return V1_1;
            }
        }

        throw new IllegalStateException("Could not resolve questionnaire version for " + questionnaire);
    }
}
