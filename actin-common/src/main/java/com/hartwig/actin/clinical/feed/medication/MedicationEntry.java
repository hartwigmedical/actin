package com.hartwig.actin.clinical.feed.medication;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MedicationEntry {

    @NotNull
    public abstract String subject();

    @NotNull
    public abstract String medicationReferenceMedicationValue();

    @NotNull
    public abstract String medicationReferenceMedicationSystem();

    @NotNull
    public abstract String codeText();

    @NotNull
    public abstract String code5ATCDisplay();

    @NotNull
    public abstract String indicationDisplay();

    @NotNull
    public abstract String dosageInstructionDoseQuantityUnit();

    public abstract double dosageInstructionDoseQuantityValue();

    @NotNull
    public abstract String dosageInstructionFrequencyUnit();

    public abstract double dosageInstructionFrequencyValue();

    public abstract double dosageInstructionMaxDosePerAdministration();

    @NotNull
    public abstract String dosageInstructionPatientInstruction();

    @NotNull
    public abstract String dosageInstructionAsNeededDisplay();

    @NotNull
    public abstract String dosageInstructionPeriodBetweenDosagesUnit();

    public abstract double dosageInstructionPeriodBetweenDosagesValue();

    @NotNull
    public abstract String dosageInstructionText();

    @NotNull
    public abstract String status();

    @NotNull
    public abstract String active();

    @NotNull
    public abstract String dosageDoseValue();

    @NotNull
    public abstract String dosageRateQuantityUnit();

    @NotNull
    public abstract String dosageDoseUnitDisplayOriginal();

    @NotNull
    public abstract LocalDate periodOfUseValuePeriodStart();

    @Nullable
    public abstract LocalDate periodOfUseValuePeriodEnd();

    @NotNull
    public abstract String stopTypeDisplay();

    @NotNull
    public abstract String categoryMedicationRequestCategoryDisplay();

    @NotNull
    public abstract String categoryMedicationRequestCategoryCodeOriginal();

}
