package com.hartwig.actin.clinical.feed.medication;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.feed.FeedUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MedicationFile {

    private static final String DELIMITER = "\t";

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy HH:mm");

    private MedicationFile() {
    }

    @NotNull
    public static List<MedicationEntry> read(@NotNull String medicationTsv) throws IOException {
        List<String> lines = FeedUtil.readFeedFile(medicationTsv);

        Map<String, Integer> fieldIndexMap = FeedUtil.createFieldIndexMap(lines.get(0), DELIMITER);
        List<MedicationEntry> entries = Lists.newArrayList();
        for (String line : lines.subList(1, lines.size())) {
            entries.add(fromLine(fieldIndexMap, line));
        }

        return entries;
    }

    @NotNull
    private static MedicationEntry fromLine(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String line) {
        String[] parts = FeedUtil.splitFeedLine(line, DELIMITER);

        return ImmutableMedicationEntry.builder()
                .subject(parts[fieldIndexMap.get("subject")])
                .medicationReferenceMedicationValue(parts[fieldIndexMap.get("medicationReference_Medication_value")])
                .medicationReferenceMedicationSystem(parts[fieldIndexMap.get("medicationReference_Medication_system")])
                .codeText(parts[fieldIndexMap.get("code_text")])
                .code5ATCDisplay(parts[fieldIndexMap.get("code5_ATC_display")])
                .indicationDisplay(parts[fieldIndexMap.get("indication_display")])
                .dosageInstructionDoseQuantityUnit(parts[fieldIndexMap.get("dosageInstruction_doseQuantity_unit")])
                .dosageInstructionDoseQuantityValue(parseDouble(parts[fieldIndexMap.get("dosageInstruction_doseQuantity_value")]))
                .dosageInstructionFrequencyUnit(parts[fieldIndexMap.get("dosageInstruction_frequency_unit")])
                .dosageInstructionFrequencyValue(parseOptionalDouble(parts[fieldIndexMap.get("dosageInstruction_frequency_value")]))
                .dosageInstructionMaxDosePerAdministration(parseOptionalDouble(parts[fieldIndexMap.get(
                        "dosageInstruction_maxDosePerAdministration")]))
                .dosageInstructionPatientInstruction(parts[fieldIndexMap.get("dosageInstruction_patientInstruction")])
                .dosageInstructionAsNeededDisplay(parts[fieldIndexMap.get("dosageInstruction_asNeeded_display")])
                .dosageInstructionPeriodBetweenDosagesUnit(parts[fieldIndexMap.get("dosageInstruction_period_between_dosages_unit")])
                .dosageInstructionPeriodBetweenDosagesValue(parseOptionalDouble(parts[fieldIndexMap.get(
                        "dosageInstruction_period_between_dosages_value")]))
                .dosageInstructionText(parts[fieldIndexMap.get("dosageInstruction_text")])
                .status(parts[fieldIndexMap.get("status")])
                .active(parts[fieldIndexMap.get("actief")])
                .dosageDoseValue(parts[fieldIndexMap.get("dosage_dose_value")])
                .dosageRateQuantityUnit(parts[fieldIndexMap.get("dosage_rateQuantity_unit")])
                .dosageDoseUnitDisplayOriginal(parts[fieldIndexMap.get("dosage_dose_unit_display_original")])
                .periodOfUseValuePeriodStart(parseDate(parts[fieldIndexMap.get("periodOfUse_valuePeriod_start")]))
                .periodOfUseValuePeriodEnd(parseOptionalDate(parts[fieldIndexMap.get("periodOfUse_valuePeriod_end")]))
                .stopTypeDisplay(parts[fieldIndexMap.get("stopType_display")])
                .categoryMedicationRequestCategoryDisplay(parts[fieldIndexMap.get("category_medicationRequestCategory_display")])
                .categoryMedicationRequestCategoryCodeOriginal(parts[fieldIndexMap.get("category_medicationRequestCategory_code_original")])
                .build();
    }

    @Nullable
    private static LocalDate parseOptionalDate(@NotNull String date) {
        return !date.isEmpty() ? parseDate(date) : null;
    }

    @NotNull
    private static LocalDate parseDate(@NotNull String date) {
        return LocalDate.parse(date, FORMAT);
    }

    @Nullable
    private static Double parseOptionalDouble(@NotNull String number) {
        return !number.isEmpty() ? parseDouble(number) : null;
    }

    private static double parseDouble(@NotNull String number) {
        String formatted = number.replace(",", ".");
        return Double.parseDouble(formatted);
    }

}
