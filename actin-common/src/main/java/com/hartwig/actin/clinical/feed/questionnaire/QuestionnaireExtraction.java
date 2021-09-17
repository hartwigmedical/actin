package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QuestionnaireExtraction {

    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final String VALUE_LIST_SEPARATOR = ",";

    private static final String TREATMENT_HISTORY_CURRENT_TUMOR = "Treatment history current tumor";
    private static final String OTHER_ONCOLOGICAL_HISTORY = "Other oncological history";
    private static final String NON_ONCOLOGICAL_HISTORY = "Non-oncological history";

    private QuestionnaireExtraction() {
    }

    @Nullable
    public static List<String> treatmentHistoriesCurrentTumor(@NotNull QuestionnaireEntry questionnaire) {
        String value = lookup(questionnaire, TREATMENT_HISTORY_CURRENT_TUMOR);
        return value != null ? Lists.newArrayList(value.split(VALUE_LIST_SEPARATOR)) : null;
    }

    @Nullable
    public static String otherOncologicalHistory(@NotNull QuestionnaireEntry questionnaire) {
        return lookup(questionnaire, OTHER_ONCOLOGICAL_HISTORY);
    }

    @Nullable
    public static String nonOncologicalHistory(@NotNull QuestionnaireEntry questionnaire) {
        return lookup(questionnaire, NON_ONCOLOGICAL_HISTORY);
    }

    @Nullable
    private static String lookup(@NotNull QuestionnaireEntry questionnaire, @NotNull String key) {
        String[] lines = questionnaire.itemAnswerValueValueString().split("\n");

        for (String line : lines) {
            if (line.contains(key)) {
                return line.substring(line.indexOf(KEY_VALUE_SEPARATOR));
            }
        }

        return null;
    }
}
