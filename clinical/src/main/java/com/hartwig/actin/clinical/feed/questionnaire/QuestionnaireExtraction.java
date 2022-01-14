package com.hartwig.actin.clinical.feed.questionnaire;

import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toOption;
import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toStage;
import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toWHO;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QuestionnaireExtraction {

    static final String KEY_VALUE_SEPARATOR = ":";
    static final String VALUE_LIST_SEPARATOR_1 = ",";
    static final String VALUE_LIST_SEPARATOR_2 = ";";

    private static final String TOXICITY_DESCRIPTION = "ONC Kuuroverzicht";
    private static final String ACTIN_QUESTIONNAIRE_KEYWORD = "ACTIN Questionnaire";

    private static final int ACTIVE_LINE_OFFSET = 1;
    private static final int SYMPTOMATIC_LINE_OFFSET = 2;

    private QuestionnaireExtraction() {
    }

    public static boolean isToxicityEntry(@NotNull QuestionnaireEntry entry) {
        return entry.description().equals(TOXICITY_DESCRIPTION);
    }

    public static boolean isActualQuestionnaire(@NotNull QuestionnaireEntry entry) {
        return entry.itemAnswerValueValueString().contains(ACTIN_QUESTIONNAIRE_KEYWORD);
    }

    @Nullable
    public static Questionnaire extract(@Nullable QuestionnaireEntry entry) {
        if (entry == null || !isActualQuestionnaire(entry)) {
            return null;
        }

        Map<QuestionnaireKey, String> mapping = QuestionnaireMapping.mapping(entry);

        LesionData brainLesionData = LesionData.forKey(entry, mapping, (QuestionnaireKey.HAS_BRAIN_LESIONS));
        LesionData cnsLesionData = LesionData.forKey(entry, mapping, (QuestionnaireKey.HAS_CNS_LESIONS));

        return ImmutableQuestionnaire.builder()
                .date(entry.authoredDateTime())
                .tumorLocation(value(entry, mapping.get(QuestionnaireKey.PRIMARY_TUMOR_LOCATION)))
                .tumorType(tumorType(entry, mapping))
                .biopsyLocation(value(entry, mapping.get(QuestionnaireKey.BIOPSY_LOCATION)))
                .stage(toStage(value(entry, mapping.get(QuestionnaireKey.STAGE))))
                .treatmentHistoryCurrentTumor(toList(value(entry, mapping.get(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR))))
                .otherOncologicalHistory(toList(value(entry, mapping.get(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY))))
                .nonOncologicalHistory(toList(value(entry, mapping.get(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY))))
                .hasMeasurableLesionRecist(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_MEASURABLE_DISEASE_RECIST))))
                .hasBrainLesions(brainLesionData.present())
                .hasActiveBrainLesions(brainLesionData.active())
                .hasSymptomaticBrainLesions(brainLesionData.symptomatic())
                .hasCnsLesions(cnsLesionData.present())
                .hasActiveCnsLesions(cnsLesionData.active())
                .hasSymptomaticCnsLesions(cnsLesionData.symptomatic())
                .hasBoneLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_BONE_LESIONS))))
                .hasLiverLesions(toOption(value(entry, mapping.get(QuestionnaireKey.HAS_LIVER_LESIONS))))
                .otherLesions(otherLesions(entry, mapping))
                .whoStatus(toWHO(value(entry, mapping.get(QuestionnaireKey.WHO_STATUS))))
                .unresolvedToxicities(toList(value(entry, mapping.get(QuestionnaireKey.UNRESOLVED_TOXICITIES))))
                .infectionStatus(toInfectionStatus(value(entry, mapping.get(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION))))
                .ecg(toECG(value(entry, mapping.get(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG))))
                .cancerRelatedComplications(toList(value(entry, mapping.get(QuestionnaireKey.CANCER_RELATED_COMPLICATIONS))))
                .build();
    }

    @Nullable
    private static InfectionStatus toInfectionStatus(@Nullable String significantCurrentInfection) {
        Boolean hasActiveInfection = null;
        if (QuestionnaireCuration.isConfiguredOption(significantCurrentInfection)) {
            hasActiveInfection = toOption(significantCurrentInfection);
        } else if (significantCurrentInfection != null && !significantCurrentInfection.isEmpty()) {
            hasActiveInfection = true;
        }

        if (hasActiveInfection == null || significantCurrentInfection == null) {
            return null;
        }

        return ImmutableInfectionStatus.builder().hasActiveInfection(hasActiveInfection).description(significantCurrentInfection).build();
    }

    @Nullable
    private static ECG toECG(@Nullable String significantAberrationLatestECG) {
        Boolean hasSignificantAberrationLatestECG = null;
        if (QuestionnaireCuration.isConfiguredOption(significantAberrationLatestECG)) {
            hasSignificantAberrationLatestECG = toOption(significantAberrationLatestECG);
        } else if (significantAberrationLatestECG != null && !significantAberrationLatestECG.isEmpty()) {
            hasSignificantAberrationLatestECG = true;
        }

        if (hasSignificantAberrationLatestECG == null || significantAberrationLatestECG == null) {
            return null;
        }

        return ImmutableECG.builder()
                .hasSigAberrationLatestECG(hasSignificantAberrationLatestECG)
                .aberrationDescription(significantAberrationLatestECG)
                .build();
    }

    @Nullable
    private static String tumorType(@NotNull QuestionnaireEntry entry, @NotNull Map<QuestionnaireKey, String> mapping) {
        QuestionnaireVersion version = QuestionnaireVersion.version(entry);
        if (version == QuestionnaireVersion.V0_1) {
            // In v0.1 we have no field yet for tumor type.
            return "Unknown";
        } else {
            return value(entry, mapping.get(QuestionnaireKey.PRIMARY_TUMOR_TYPE));
        }
    }

    @Nullable
    private static List<String> otherLesions(@NotNull QuestionnaireEntry entry, @NotNull Map<QuestionnaireKey, String> mapping) {
        QuestionnaireVersion version = QuestionnaireVersion.version(entry);
        if (version == QuestionnaireVersion.V0_1) {
            //In v0.1, the format for primary tumor location is "$location ($otherLesions)"
            String input = value(entry, mapping.get(QuestionnaireKey.PRIMARY_TUMOR_LOCATION));
            if (input.contains("(") && input.contains(")")) {
                int start = input.indexOf("(");
                int end = input.indexOf(")");
                return toList(input.substring(start + 1, end));
            } else {
                return null;
            }
        } else {
            return toList(value(entry, mapping.get(QuestionnaireKey.OTHER_LESIONS)));
        }
    }

    @Nullable
    private static List<String> toList(@Nullable String value) {
        if (value == null) {
            return null;
        }

        String reformatted = value.replaceAll(VALUE_LIST_SEPARATOR_2, VALUE_LIST_SEPARATOR_1);
        String[] split = reformatted.split(VALUE_LIST_SEPARATOR_1);

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

        String[] lines = QuestionnaireReader.read(entry);

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(key)) {
                return new LookupResult(lines, i);
            }
        }

        throw new IllegalStateException("Could not find key " + key + " in questionnaire " + entry);
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

    private static class LesionData {

        @Nullable
        private final Boolean present;
        @Nullable
        private final Boolean active;
        @Nullable
        private final Boolean symptomatic;

        public LesionData(@Nullable final Boolean present, @Nullable final Boolean active, @Nullable final Boolean symptomatic) {
            this.present = present;
            this.active = active;
            this.symptomatic = symptomatic;
        }

        @NotNull
        static LesionData forKey(@NotNull QuestionnaireEntry entry, @NotNull Map<QuestionnaireKey, String> mapping,
                @NotNull QuestionnaireKey key) {
            Boolean present = toOption(value(entry, mapping.get(key)));
            Boolean active = null;
            Boolean symptomatic = null;
            if (present != null) {
                active = present ? toOption(value(entry, mapping.get(key), ACTIVE_LINE_OFFSET)) : false;
                symptomatic = present ? toOption(value(entry, mapping.get(key), SYMPTOMATIC_LINE_OFFSET)) : false;
            }

            return new LesionData(present, active, symptomatic);
        }

        @Nullable
        public Boolean present() {
            return present;
        }

        @Nullable
        public Boolean active() {
            return active;
        }

        @Nullable
        public Boolean symptomatic() {
            return symptomatic;
        }
    }
}
