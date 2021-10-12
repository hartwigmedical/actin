package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.datamodel.clinical.Medication;

import org.jetbrains.annotations.NotNull;

public class MedicationComparator implements Comparator<Medication> {

    @Override
    public int compare(@NotNull Medication medication1, @NotNull Medication medication2) {
        return medication1.name().compareTo(medication2.name());
    }
}

