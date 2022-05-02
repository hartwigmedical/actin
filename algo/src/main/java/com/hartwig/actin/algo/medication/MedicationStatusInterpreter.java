package com.hartwig.actin.algo.medication;

import com.hartwig.actin.clinical.datamodel.Medication;

import org.jetbrains.annotations.NotNull;

public interface MedicationStatusInterpreter {

    @NotNull
    MedicationStatusInterpretation interpret(@NotNull Medication medication);
}
