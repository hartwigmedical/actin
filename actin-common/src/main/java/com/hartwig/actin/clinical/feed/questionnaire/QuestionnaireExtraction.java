package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QuestionnaireExtraction {

    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final String VALUE_LIST_SEPARATOR_1 = ",";
    private static final String VALUE_LIST_SEPARATOR_2 = ";";

    private static final String ACTIN_QUESTIONNAIRE_KEYWORD = "ACTIN Questionnaire";

    private static final int ACTIVE_LINE_OFFSET = 1;
    private static final int SYMPTOMATIC_LINE_OFFSET = 2;

    private QuestionnaireExtraction() {
    }

    public static boolean isActualQuestionnaire(@NotNull QuestionnaireEntry entry) {
        return entry.itemAnswerValueValueString().contains(ACTIN_QUESTIONNAIRE_KEYWORD);
    }

    @Nullable
    public static String tumorLocation(@NotNull QuestionnaireEntry questionnaire) {
        return value(questionnaire, QuestionnaireKey.PRIMARY_TUMOR_LOCATION);
    }

    @Nullable
    public static String tumorType(@NotNull QuestionnaireEntry questionnaire) {
        return value(questionnaire, QuestionnaireKey.PRIMARY_TUMOR_TYPE);
    }

    @Nullable
    public static TumorStage stage(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseStage(value(questionnaire, QuestionnaireKey.STAGE));
    }

    @Nullable
    public static Boolean hasMeasurableLesionRecist(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST));
    }

    @Nullable
    public static Boolean hasBrainLesions(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_BRAIN_LESIONS));
    }

    @Nullable
    public static Boolean hasActiveBrainLesions(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_BRAIN_LESIONS, ACTIVE_LINE_OFFSET));
    }

    @Nullable
    public static Boolean hasSymptomaticBrainLesions(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_BRAIN_LESIONS, SYMPTOMATIC_LINE_OFFSET));
    }

    @Nullable
    public static Boolean hasCnsLesions(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_CNS_LESIONS));
    }

    @Nullable
    public static Boolean hasActiveCnsLesions(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_CNS_LESIONS, ACTIVE_LINE_OFFSET));
    }

    @Nullable
    public static Boolean hasSymptomaticCnsLesions(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_CNS_LESIONS, SYMPTOMATIC_LINE_OFFSET));
    }

    @Nullable
    public static Boolean hasBoneLesions(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_BONE_LESIONS));
    }

    @Nullable
    public static Boolean hasLiverLesions(@NotNull QuestionnaireEntry questionnaire) {
        return QuestionnaireUtil.parseOption(value(questionnaire, QuestionnaireKey.HAS_LIVER_LESIONS));
    }

    @Nullable
    public static Boolean hasOtherLesions(@NotNull QuestionnaireEntry questionnaire) {
        // TODO Coming in later version.
        return null;
    }

    @Nullable
    public static String otherLesions(@NotNull QuestionnaireEntry questionnaire) {
        // TODO Coming in later version.
        return null;
    }

    @Nullable
    public static List<String> treatmentHistoriesCurrentTumor(@NotNull QuestionnaireEntry questionnaire) {
        return toList(value(questionnaire, QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR));
    }

    @Nullable
    private static List<String> toList(@Nullable String value) {
        if (value == null) {
            return null;
        }

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

    @Nullable
    private static String value(@NotNull QuestionnaireEntry questionnaire, @NotNull QuestionnaireKey key) {
        return value(questionnaire, key, 0);
    }

    @Nullable
    private static String value(@NotNull QuestionnaireEntry questionnaire, @NotNull QuestionnaireKey key, int lineOffset) {
        LookupResult result = lookup(questionnaire, key);

        String line = result != null ? result.lines[result.lineIndex + lineOffset] : null;
        return line != null ? line.substring(line.indexOf(KEY_VALUE_SEPARATOR) + 1).trim() : null;
    }

    @Nullable
    private static LookupResult lookup(@NotNull QuestionnaireEntry questionnaire, @NotNull QuestionnaireKey key) {
        Map<QuestionnaireKey, String> mapping = QuestionnaireMapping.mapping(questionnaire);

        String keyValue = mapping.get(key);
        if (keyValue == null) {
            return null;
        } else {
            String[] lines = questionnaire.itemAnswerValueValueString().split("\n");

            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains(keyValue)) {
                    return new LookupResult(lines, i);
                }
            }
        }

        throw new IllegalStateException("Could not find key " + key + " in questionnaire " + questionnaire);
    }

    private static class LookupResult {

        @NotNull
        private final String[] lines;
        private final int lineIndex;

        public LookupResult(@NotNull final String[] lines, final int lineIndex) {
            this.lines = lines;
            this.lineIndex = lineIndex;
        }
    }
}
