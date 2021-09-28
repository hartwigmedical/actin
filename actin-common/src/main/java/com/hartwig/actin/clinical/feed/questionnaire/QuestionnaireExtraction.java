package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
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

        String significantAberrationLatestECG = value(entry, mapping.get(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG));

        Boolean hasSignificantAberrationLatestECG = significantAberrationLatestECG != null && !significantAberrationLatestECG.isEmpty();
        if (isConfiguredOption(significantAberrationLatestECG)) {
            hasSignificantAberrationLatestECG = toOption(significantAberrationLatestECG);
            if (hasSignificantAberrationLatestECG == null) {
                significantAberrationLatestECG = null;
            } else if (!hasSignificantAberrationLatestECG) {
                significantAberrationLatestECG = Strings.EMPTY;
            }
        }
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
                .hasSignificantCurrentInfection(toOption(value(entry, mapping.get(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION))))
                .hasSignificantAberrationLatestECG(hasSignificantAberrationLatestECG)
                .significantAberrationLatestECG(significantAberrationLatestECG)
                .build();
    }

    @Nullable
    @VisibleForTesting
    static TumorStage toStage(@Nullable String stage) {
        if (stage == null || stage.isEmpty()) {
            return null;
        }

        if (!QuestionnaireConstants.STAGE_MAPPING.containsKey(stage)) {
            LOGGER.warn("Unrecognized questionnaire tumor stage: '{}'", stage);
            return null;
        }

        return QuestionnaireConstants.STAGE_MAPPING.get(stage);
    }

    @Nullable
    @VisibleForTesting
    static Boolean toOption(@Nullable String option) {
        if (option == null || option.isEmpty()) {
            return null;
        }

        if (!isConfiguredOption(option)) {
            LOGGER.warn("Unrecognized questionnaire option: '{}'", option);
            return null;
        }

        return QuestionnaireConstants.OPTION_MAPPING.get(option);
    }

    private static boolean isConfiguredOption(@Nullable String option) {
        if (option == null) {
            return false;
        }

        return QuestionnaireConstants.OPTION_MAPPING.containsKey(option);
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
    private static String value(@NotNull QuestionnaireEntry entry, @Nullable String key) {
        return value(entry, key, 0);
    }

    @Nullable
    private static String value(@NotNull QuestionnaireEntry entry, @Nullable String key, int lineOffset) {
        LookupResult result = lookup(entry, key);

        String line = result != null ? result.lines[result.lineIndex + lineOffset] : null;
        return line != null ? line.substring(line.indexOf(KEY_VALUE_SEPARATOR) + 1).trim() : null;
    }

    @Nullable
    private static LookupResult lookup(@NotNull QuestionnaireEntry entry, @Nullable String key) {
        if (key == null) {
            return null;
        }

        String[] lines = read(entry);

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(key)) {
                return new LookupResult(lines, i);
            }
        }

        throw new IllegalStateException("Could not find key " + key + " in questionnaire " + entry);
    }

    @NotNull
    private static String[] read(@NotNull QuestionnaireEntry entry) {
        String[] lines = entry.itemAnswerValueValueString().split("\n");
        String[] cleaned = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            String clean = lines[i];
            for (String term : QuestionnaireConstants.TERMS_TO_CLEAN) {
                while (clean.contains(term)) {
                    clean = clean.replace(term, Strings.EMPTY);
                }
            }
            cleaned[i] = clean;
        }
        return cleaned;
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
