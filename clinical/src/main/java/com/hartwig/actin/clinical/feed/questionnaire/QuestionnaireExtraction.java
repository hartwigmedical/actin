package com.hartwig.actin.clinical.feed.questionnaire;

import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toOption;
import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toStage;
import static com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCuration.toWHO;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.ImmutableECG;
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus;
import com.hartwig.actin.clinical.datamodel.InfectionStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
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
        String questionnaireContents = QuestionnaireReader.cleanedContents(entry);

        Map<Integer, QuestionnaireKey> keyByPositions = mapping.entrySet()
                .stream()
                .filter(fieldLookup -> fieldLookup.getValue() != null)
                .map(fieldLookup -> Map.entry(questionnaireContents.indexOf(fieldLookup.getValue()), fieldLookup.getKey()))
                .peek(positionToKey -> {
                    if (positionToKey.getKey() == -1) {
                        if (positionToKey.getValue() == QuestionnaireKey.GENAYA_SUBJECT_NUMBER) {
                            LOGGER.debug("Key '{}' not present but skipped since it is configured as optional in questionnaire '{}'",
                                    positionToKey.getValue(),
                                    entry);
                        } else {
                            throw new IllegalStateException(
                                    "Could not find key " + positionToKey.getValue() + " in questionnaire " + entry);
                        }
                    }
                })
                .filter(positionToKey -> positionToKey.getKey() != -1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Integer> fieldStartPositions = keyByPositions.keySet().stream().sorted().collect(Collectors.toList());

        Map<QuestionnaireKey, String> rawValuesByKey = IntStream.range(0, fieldStartPositions.size()).peek(i -> {
            int fieldStartPosition = fieldStartPositions.get(i);
            QuestionnaireKey questionnaireKey = keyByPositions.get(fieldStartPosition);
            int valueStartPosition = fieldStartPosition + mapping.get(questionnaireKey).length() + 1;
            int nextFieldStartPosition =
                    (i < fieldStartPositions.size() - 1) ? fieldStartPositions.get(i + 1) : questionnaireContents.length();
            System.out.printf("%s: (%s, %s) -> %s%n",
                    questionnaireKey,
                    valueStartPosition,
                    nextFieldStartPosition,
                    questionnaireContents.substring(valueStartPosition, nextFieldStartPosition));
        }).mapToObj(i -> {
            int fieldStartPosition = fieldStartPositions.get(i);
            QuestionnaireKey questionnaireKey = keyByPositions.get(fieldStartPosition);
            int valueStartPosition = fieldStartPosition + mapping.get(questionnaireKey).length() + 1;
            int nextFieldStartPosition =
                    (i < fieldStartPositions.size() - 1) ? fieldStartPositions.get(i + 1) : questionnaireContents.length();
            return Map.entry(questionnaireKey, questionnaireContents.substring(valueStartPosition, nextFieldStartPosition).trim());
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        LesionData brainLesionData = LesionData.fromString(rawValuesByKey.get(QuestionnaireKey.HAS_BRAIN_LESIONS));
        LesionData cnsLesionData = LesionData.fromString(rawValuesByKey.get(QuestionnaireKey.HAS_CNS_LESIONS));

        return ImmutableQuestionnaire.builder()
                .date(entry.authored())
                .tumorLocation(rawValuesByKey.get(QuestionnaireKey.PRIMARY_TUMOR_LOCATION))
                .tumorType(QuestionnaireVersion.version(entry) == QuestionnaireVersion.V0_1
                        ? "Unknown"
                        : rawValuesByKey.get(QuestionnaireKey.PRIMARY_TUMOR_TYPE))
                .biopsyLocation(rawValuesByKey.get(QuestionnaireKey.BIOPSY_LOCATION))
                .stage(toStage(rawValuesByKey.get(QuestionnaireKey.STAGE)))
                .treatmentHistoryCurrentTumor(toList(rawValuesByKey.get(QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR)))
                .otherOncologicalHistory(toList(rawValuesByKey.get(QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY)))
                .secondaryPrimaries(toSecondaryPrimaries(rawValuesByKey.get(QuestionnaireKey.SECONDARY_PRIMARY)))
                .nonOncologicalHistory(toList(rawValuesByKey.get(QuestionnaireKey.NON_ONCOLOGICAL_HISTORY)))
                .ihcTestResults(toList(rawValuesByKey.get(QuestionnaireKey.IHC_TEST_RESULTS)))
                .pdl1TestResults(toList(rawValuesByKey.get(QuestionnaireKey.PDL1_TEST_RESULTS)))
                .hasMeasurableDisease(toOption(rawValuesByKey.get(QuestionnaireKey.HAS_MEASURABLE_DISEASE)))
                .hasBrainLesions(brainLesionData.present())
                .hasActiveBrainLesions(brainLesionData.active())
                .hasCnsLesions(cnsLesionData.present())
                .hasActiveCnsLesions(cnsLesionData.active())
                .hasBoneLesions(toOption(rawValuesByKey.get(QuestionnaireKey.HAS_BONE_LESIONS)))
                .hasLiverLesions(toOption(rawValuesByKey.get(QuestionnaireKey.HAS_LIVER_LESIONS)))
                .otherLesions(otherLesions(entry, rawValuesByKey))
                .whoStatus(toWHO(rawValuesByKey.get(QuestionnaireKey.WHO_STATUS)))
                .unresolvedToxicities(toList(rawValuesByKey.get(QuestionnaireKey.UNRESOLVED_TOXICITIES)))
                .infectionStatus(toInfectionStatus(rawValuesByKey.get(QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION)))
                .ecg(toECG(rawValuesByKey.get(QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG)))
                .complications(toList(rawValuesByKey.get(QuestionnaireKey.COMPLICATIONS)))
                .genayaSubjectNumber(rawValuesByKey.get(QuestionnaireKey.GENAYA_SUBJECT_NUMBER))
                .build();
    }

    @Nullable
    private static List<String> toSecondaryPrimaries(@Nullable String input) {
        if (input == null) {
            return null;
        }
        String[] tokens = input.split("- Last date of active treatment:");
        String secondaryPrimary = tokens.length > 0 ? tokens[0].trim() : Strings.EMPTY;
        if (secondaryPrimary.isEmpty()) {
            return null;
        }

        if (tokens.length >= 2) {
            String lastTreatmentInfo = tokens[1].trim();
            if (!lastTreatmentInfo.isEmpty()) {
                return List.of(secondaryPrimary + " | " + lastTreatmentInfo);
            }
        }
        return List.of(secondaryPrimary);
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
    private static List<String> otherLesions(@NotNull QuestionnaireEntry entry, @NotNull Map<QuestionnaireKey, String> rawValuesByKey) {
        QuestionnaireVersion version = QuestionnaireVersion.version(entry);
        if (version == QuestionnaireVersion.V0_1) {
            //In v0.1, the format for primary tumor location is "$location ($otherLesions)"
            String input = rawValuesByKey.get(QuestionnaireKey.PRIMARY_TUMOR_LOCATION);
            if (input.contains("(") && input.contains(")")) {
                int start = input.indexOf("(");
                int end = input.indexOf(")");
                return toList(input.substring(start + 1, end));
            } else {
                return null;
            }
        } else {
            return toList(rawValuesByKey.get(QuestionnaireKey.OTHER_LESIONS));
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
        return Arrays.stream(values).map(String::trim).filter(str -> !str.isEmpty()).collect(Collectors.toList());
    }

    private static class LesionData {

        @Nullable
        private final Boolean present;
        @Nullable
        private final Boolean active;

        public LesionData(@Nullable final Boolean present, @Nullable final Boolean active) {
            this.present = present;
            this.active = active;
        }

        @NotNull
        static LesionData fromString(@NotNull String input) {
            String[] tokens = input.split(".Active:");
            Boolean present = tokens.length > 0 ? toOption(tokens[0].trim()) : null;
            Boolean active = null;
            if (present != null && tokens.length >= 2) {
                String remainingText = tokens[1].trim();
                String activeOptionText =
                        remainingText.contains(" ") ? remainingText.substring(0, remainingText.indexOf(" ")) : remainingText;
                Boolean activeOption = toOption(activeOptionText);
                if (activeOption != null) {
                    active = present ? activeOption : false;
                }
            }

            return new LesionData(present, active);
        }

        @Nullable
        public Boolean present() {
            return present;
        }

        @Nullable
        public Boolean active() {
            return active;
        }
    }
}
