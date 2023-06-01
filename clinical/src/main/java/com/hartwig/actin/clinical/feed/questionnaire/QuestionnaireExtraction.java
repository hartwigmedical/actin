package com.hartwig.actin.clinical.feed.questionnaire;

import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toOption;
import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toStage;
import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toWHO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QuestionnaireExtraction {

    private static final Logger LOGGER = LogManager.getLogger(QuestionnaireExtraction.class);

    static final String KEY_VALUE_SEPARATOR = ":";
    static final String VALUE_LIST_SEPARATOR_1 = ",";
    static final String VALUE_LIST_SEPARATOR_2 = ";";

    private static final String BLOOD_TRANSFUSION_DESCRIPTION = "Aanvraag bloedproducten_test";
    private static final String TOXICITY_DESCRIPTION = "ONC Kuuroverzicht";
    private static final String ACTIN_QUESTIONNAIRE_KEYWORD = "ACTIN Questionnaire";

    private static final int ACTIVE_LINE_OFFSET = 1;

    private QuestionnaireExtraction() {
    }

    public static boolean isBloodTransfusionEntry(@NotNull QuestionnaireEntry entry) {
        return entry.description().equals(BLOOD_TRANSFUSION_DESCRIPTION);
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
        String[] lines = QuestionnaireReader.read(entry, QuestionnaireMapping.keyStrings(entry));

        LesionData brainLesionData = lesionData(lines, mapping.get(QuestionnaireKey.HAS_BRAIN_LESIONS));
        LesionData cnsLesionData = lesionData(lines, mapping.get(QuestionnaireKey.HAS_CNS_LESIONS));

        return ImmutableQuestionnaire.builder()
                .date(entry.authored())
                .tumorLocation(value(lines, mapping.get(QuestionnaireKey.PRIMARY_TUMOR_LOCATION)))
                .tumorType(QuestionnaireVersion.version(entry) == QuestionnaireVersion.V0_1
                        ? "Unknown"
                        : value(lines, mapping.get(QuestionnaireKey.PRIMARY_TUMOR_TYPE)))
                .biopsyLocation(value(lines, mapping.get(QuestionnaireKey.BIOPSY_LOCATION)))
                .stage(toStage(value(lines, mapping.get(QuestionnaireKey.STAGE))))
                .treatmentHistoryCurrentTumor(toList(value(lines, mapping.get(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR))))
                .otherOncologicalHistory(toList(value(lines, mapping.get(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY))))
                .secondaryPrimaries(secondaryPrimaries(lines, mapping.get(QuestionnaireKey.SECONDARY_PRIMARY)))
                .nonOncologicalHistory(toList(value(lines, mapping.get(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY))))
                .ihcTestResults(toList(value(lines, mapping.get(QuestionnaireKey.IHC_TEST_RESULTS))))
                .pdl1TestResults(toList(value(lines, mapping.get(QuestionnaireKey.PDL1_TEST_RESULTS))))
                .hasMeasurableDisease(toOption(value(lines, mapping.get(QuestionnaireKey.HAS_MEASURABLE_DISEASE))))
                .hasBrainLesions(brainLesionData.present())
                .hasActiveBrainLesions(brainLesionData.active())
                .hasCnsLesions(cnsLesionData.present())
                .hasActiveCnsLesions(cnsLesionData.active())
                .hasBoneLesions(toOption(value(lines, mapping.get(QuestionnaireKey.HAS_BONE_LESIONS))))
                .hasLiverLesions(toOption(value(lines, mapping.get(QuestionnaireKey.HAS_LIVER_LESIONS))))
                .otherLesions(otherLesions(entry, lines, mapping))
                .whoStatus(toWHO(value(lines, mapping.get(QuestionnaireKey.WHO_STATUS))))
                .unresolvedToxicities(toList(value(lines, mapping.get(QuestionnaireKey.UNRESOLVED_TOXICITIES))))
                .infectionStatus(QuestionnaireCuration.toInfectionStatus(value(lines,
                        mapping.get(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION))))
                .ecg(QuestionnaireCuration.toECG(value(lines, mapping.get(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG))))
                .complications(toList(value(lines, mapping.get(QuestionnaireKey.COMPLICATIONS))))
                .genayaSubjectNumber(optionalValue(lines, mapping.get(QuestionnaireKey.GENAYA_SUBJECT_NUMBER)))
                .build();
    }

    @Nullable
    private static List<String> secondaryPrimaries(@NotNull String[] lines, @Nullable String secondaryPrimaryKey) {
        List<String> extractedValues = (secondaryPrimaryKey == null) ? null : values(lines, secondaryPrimaryKey, 1);
        if (extractedValues == null || extractedValues.get(0).isEmpty()) {
            return null;
        }
        return QuestionnaireCuration.toSecondaryPrimaries(extractedValues.get(0), extractedValues.get(1));
    }

    @Nullable
    private static List<String> otherLesions(@NotNull QuestionnaireEntry entry, @NotNull String[] lines,
            @NotNull Map<QuestionnaireKey, String> mapping) {
        QuestionnaireVersion version = QuestionnaireVersion.version(entry);
        if (version == QuestionnaireVersion.V0_1) {
            //In v0.1, the format for primary tumor location is "$location ($otherLesions)"
            String input = value(lines, mapping.get(QuestionnaireKey.PRIMARY_TUMOR_LOCATION));
            if (input.contains("(") && input.contains(")")) {
                int start = input.indexOf("(");
                int end = input.indexOf(")");
                return toList(input.substring(start + 1, end));
            } else {
                return null;
            }
        } else {
            return toList(value(lines, mapping.get(QuestionnaireKey.OTHER_LESIONS)));
        }
    }

    private static LesionData lesionData(@NotNull String[] lines, @NotNull String keyString) {
        List<String> extractedValues = values(lines, keyString, ACTIVE_LINE_OFFSET);
        return (extractedValues == null)
                ? new LesionData(null, null)
                : LesionData.fromString(extractedValues.get(0), extractedValues.get(1));
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
    private static String value(@NotNull String[] lines, @Nullable String key) {
        return value(lines, key, false);
    }

    @Nullable
    private static String optionalValue(@NotNull String[] lines, @Nullable String key) {
        return value(lines, key, true);
    }

    @Nullable
    private static String value(@NotNull String[] lines, @Nullable String key, boolean isOptional) {
        Integer lineIndex = lookup(lines, key, isOptional);

        String line = lineIndex != null ? lines[lineIndex] : null;
        return line != null ? extractValue(line) : null;
    }

    @Nullable
    private static List<String> values(@NotNull String[] lines, @Nullable String key, int lineOffset) {
        Integer lineIndex = lookup(lines, key, false);
        if (lineIndex != null) {
            List<String> extractedValues = Arrays.stream(lines, lineIndex, lineIndex + lineOffset + 1)
                    .map(QuestionnaireExtraction::extractValue)
                    .collect(Collectors.toList());

            if (extractedValues.size() < lineOffset + 1) {
                throw new RuntimeException(String.format("Failed to extract %d lines for key '%s'", lineOffset + 1, key));
            }
            return extractedValues;
        }
        return null;
    }

    @NotNull
    private static String extractValue(String line) {
        return line.substring(line.indexOf(KEY_VALUE_SEPARATOR) + 1).trim();
    }

    @Nullable
    private static Integer lookup(@NotNull String[] lines, @Nullable String key, boolean isOptional) {
        if (key == null) {
            return null;
        }

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains(key)) {
                return i;
            }
        }

        if (isOptional) {
            LOGGER.debug("Key '{}' not present but skipped since it is configured as optional in questionnaire '{}'", key,
                    String.join("\n", lines));
            return null;
        }

        throw new IllegalStateException("Could not find key " + key + " in questionnaire " + String.join("\n", lines));
    }
}
