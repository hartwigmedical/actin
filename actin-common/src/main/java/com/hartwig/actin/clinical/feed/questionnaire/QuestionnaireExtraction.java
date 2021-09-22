package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QuestionnaireExtraction {

    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final String VALUE_LIST_SEPARATOR_1 = ",";
    private static final String VALUE_LIST_SEPARATOR_2 = ";";

    private static final String ACTIN_QUESTIONNAIRE_KEYWORD = "ACTIN Questionnaire";

    private QuestionnaireExtraction() {
    }

    public static boolean isActualQuestionnaire(@NotNull QuestionnaireEntry entry) {
        return entry.itemAnswerValueValueString().contains(ACTIN_QUESTIONNAIRE_KEYWORD);
    }

    @NotNull
    public static List<String> treatmentHistoriesCurrentTumor(@NotNull QuestionnaireEntry questionnaire) {
        return toList(lookup(questionnaire, QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR));
    }

    @Nullable
    public static String otherOncologicalHistory(@NotNull QuestionnaireEntry questionnaire) {
        return lookup(questionnaire, QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY);
    }

    @Nullable
    public static String nonOncologicalHistory(@NotNull QuestionnaireEntry questionnaire) {
        return lookup(questionnaire, QuestionnaireKey.NON_ONCOLOGICAL_HISTORY);
    }

    @NotNull
    private static List<String> toList(@Nullable String value) {
        String[] split;
        if (value.contains(VALUE_LIST_SEPARATOR_1)) {
            split = value.split(VALUE_LIST_SEPARATOR_1);
        } else {
            split = value.split(VALUE_LIST_SEPARATOR_2);
        }

        return cleanAndTrim(split);
    }

    @NotNull
    private static List<String> cleanAndTrim(@NotNull String[] values) {
        List<String> trimmed = Lists.newArrayList();
        for (String value : values) {
            String trim = value.trim();
            if (!trim.isEmpty()) {
                trimmed.add(trim);
            }
        }
        return trimmed;
    }

    @NotNull
    private static String lookup(@NotNull QuestionnaireEntry questionnaire, @NotNull QuestionnaireKey key) {
        Map<QuestionnaireKey, String> mapping = QuestionnaireMapping.mapping(questionnaire);

        String keyValue = mapping.get(key);
        if (keyValue == null) {
            return Strings.EMPTY;
        } else {
            String[] lines = questionnaire.itemAnswerValueValueString().split("\n");

            for (String line : lines) {
                if (line.contains(keyValue)) {
                    return line.substring(line.indexOf(KEY_VALUE_SEPARATOR) + 1).trim();
                }
            }
        }

        throw new IllegalStateException("Could not find key " + key + " in questionnaire " + questionnaire);
    }
}
