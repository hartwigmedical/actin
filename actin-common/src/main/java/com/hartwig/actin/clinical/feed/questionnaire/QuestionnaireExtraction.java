package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QuestionnaireExtraction {

    private static final Logger LOGGER = LogManager.getLogger(QuestionnaireExtraction.class);

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
    public static Questionnaire extract(@Nullable QuestionnaireEntry entry) {
        if (entry == null) {
            return null;
        }

        Map<QuestionnaireKey, String> mapping = QuestionnaireMapping.mapping(entry);

        return ImmutableQuestionnaire.builder()
                .tumorLocation(value(entry, mapping.get(QuestionnaireKey.PRIMARY_TUMOR_LOCATION)))
                .tumorType(value(entry, mapping.get(QuestionnaireKey.PRIMARY_TUMOR_TYPE)))
                .stage(toStage(value(entry, mapping.get(QuestionnaireKey.STAGE))))
                .treatmentHistoriesCurrentTumor(toList(value(entry, mapping.get(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR))))
                .hasMeasurableLesionRecist(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST))))
                .hasBrainLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_BRAIN_LESIONS))))
                .hasActiveBrainLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_BRAIN_LESIONS), ACTIVE_LINE_OFFSET)))
                .hasSymptomaticBrainLesions(toOption(value(entry,
                        mapping.get(QuestionnaireKey.HAS_BRAIN_LESIONS),
                        SYMPTOMATIC_LINE_OFFSET)))
                .hasCnsLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_CNS_LESIONS))))
                .hasActiveCnsLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_CNS_LESIONS), ACTIVE_LINE_OFFSET)))
                .hasSymptomaticCnsLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_CNS_LESIONS), SYMPTOMATIC_LINE_OFFSET)))
                .hasBoneLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_BONE_LESIONS))))
                .hasLiverLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_LIVER_LESIONS))))
                .whoStatus(toWHO(value(entry, mapping.get(QuestionnaireKey.WHO_STATUS))))
                .significantCurrentInfection(value(entry, mapping.get(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION)))
                .significantAberrationLatestECG(value(entry, mapping.get(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG)))
                .build();
    }

    @Nullable
    @VisibleForTesting
    static TumorStage toStage(@Nullable String stage) {
        if (stage == null || stage.isEmpty()) {
            return null;
        }

        switch (stage) {
            case "II":
            case "2":
                return TumorStage.II;
            case "IIb":
                return TumorStage.IIB;
            case "III":
            case "3":
                return TumorStage.III;
            case "IIIc":
                return TumorStage.IIIC;
            case "IV":
            case "4":
                return TumorStage.IV;
            default: {
                LOGGER.warn("Unrecognized questionnaire tumor stage: '{}'", stage);
                return null;
            }
        }
    }

    @Nullable
    @VisibleForTesting
    static Boolean toOption(@Nullable String option) {
        if (option == null || option.isEmpty()) {
            return null;
        }

        switch (option.toLowerCase()) {
            case "no":
                return false;
            case "yes":
                return true;
            case "n.v.t.":
            case "nvt":
            case "nvt.":
            case "n.v.t":
            case "unknown":
            case "-":
                return null;
            default: {
                LOGGER.warn("Unrecognized questionnaire option: '{}'", option);
                return null;
            }
        }
    }

    @Nullable
    @VisibleForTesting
    static Integer toWHO(@Nullable String integer) {
        if (integer == null || integer.isEmpty()) {
            return null;
        }

        int value = Integer.parseInt(integer);
        if (value >= 0 && value <= 4) {
            return value;
        } else {
            LOGGER.warn("Unrecognized WHO status: '{}'", value);
            return null;
        }
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
    private static String value(@NotNull QuestionnaireEntry questionnaire, @Nullable String key) {
        return value(questionnaire, key, 0);
    }

    @Nullable
    private static String value(@NotNull QuestionnaireEntry questionnaire, @Nullable String key, int lineOffset) {
        LookupResult result = lookup(questionnaire, key);

        String line = result != null ? result.lines[result.lineIndex + lineOffset] : null;
        return line != null ? line.substring(line.indexOf(KEY_VALUE_SEPARATOR) + 1).trim() : null;
    }

    @Nullable
    private static LookupResult lookup(@NotNull QuestionnaireEntry questionnaire, @Nullable String key) {
        if (key == null) {
            return null;
        }

        String[] lines = questionnaire.itemAnswerValueValueString().split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(key)) {
                return new LookupResult(lines, i);
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
