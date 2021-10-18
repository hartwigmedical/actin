package com.hartwig.actin.clinical.feed.medication;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.clinical.feed.FeedEntryCreator;
import com.hartwig.actin.clinical.feed.FeedLine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MedicationEntryCreator implements FeedEntryCreator<MedicationEntry> {

    public MedicationEntryCreator() {
    }

    @NotNull
    @Override
    public MedicationEntry fromLine(@NotNull final FeedLine line) {
        return ImmutableMedicationEntry.builder()
                .subject(line.string("subject"))
                .medicationReferenceMedicationValue(line.string("medicationReference_Medication_value"))
                .medicationReferenceMedicationSystem(line.string("medicationReference_Medication_system"))
                .codeText(line.string("code_text"))
                .code5ATCDisplay(line.string("code5_ATC_display"))
                .indicationDisplay(line.string("indication_display"))
                .dosageInstructionDoseQuantityUnit(line.string("dosageInstruction_doseQuantity_unit"))
                .dosageInstructionDoseQuantityValue(line.number("dosageInstruction_doseQuantity_value"))
                .dosageInstructionFrequencyUnit(line.string("dosageInstruction_frequency_unit"))
                .dosageInstructionFrequencyValue(line.optionalNumber("dosageInstruction_frequency_value"))
                .dosageInstructionMaxDosePerAdministration(line.optionalNumber("dosageInstruction_maxDosePerAdministration"))
                .dosageInstructionPatientInstruction(line.string("dosageInstruction_patientInstruction"))
                .dosageInstructionAsNeededDisplay(line.string("dosageInstruction_asNeeded_display"))
                .dosageInstructionPeriodBetweenDosagesUnit(line.string("dosageInstruction_period_between_dosages_unit"))
                .dosageInstructionPeriodBetweenDosagesValue(line.optionalNumber("dosageInstruction_period_between_dosages_value"))
                .dosageInstructionText(line.string("dosageInstruction_text"))
                .status(line.string("status"))
                .active(isActive(line.string("active")))
                .dosageDoseValue(line.string("dosage_dose_value"))
                .dosageRateQuantityUnit(line.string("dosage_rateQuantity_unit"))
                .dosageDoseUnitDisplayOriginal(line.string("dosage_dose_unit_display_original"))
                .periodOfUseValuePeriodStart(line.date("periodOfUse_valuePeriod_start"))
                .periodOfUseValuePeriodEnd(line.optionalDate("periodOfUse_valuePeriod_end"))
                .stopTypeDisplay(line.string("stopType_display"))
                .categoryMedicationRequestCategoryDisplay(line.string("category_medicationRequestCategory_display"))
                .categoryMedicationRequestCategoryCodeOriginal(line.string("category_medicationRequestCategory_code_original"))
                .build();
    }

    @Override
    public boolean isValid(@NotNull final FeedLine line) {
        return true;
    }

    @Nullable
    @VisibleForTesting
    static Boolean isActive(@NotNull String activeField) {
        // This field seems like a left-join where either the value is "active" or "null" in which case we can assume false.
        if (activeField.equalsIgnoreCase("null")) {
            return false;
        } else if (activeField.equals("active")) {
            return true;
        }

        return null;
    }
}
