package com.hartwig.actin.clinical.feed.questionnaire;

import org.jetbrains.annotations.NotNull;

enum QuestionnaireVersion {
    V0_1,
    V0_2,
    V1_0,
    V1_1;

    @NotNull
    public static QuestionnaireVersion version(@NotNull QuestionnaireEntry entry) {
        String[] lines = entry.itemAnswerValueValueString().split("\n");

        for (String line : lines) {
            if (line.contains("Other (e.g. Osteoporosis, Pleural effusion)")) {
                return V0_1;
            } else if (line.contains("Other (e.g. pleural effusion)")) {
                return V0_2;
            } else if (line.contains("\\li0\\ri0")) {
                return V1_0;
            } else if (line.contains("- Active:")) {
                return V1_1;
            }
        }

        throw new IllegalStateException("Could not resolve questionnaire version for " + entry);
    }
}
