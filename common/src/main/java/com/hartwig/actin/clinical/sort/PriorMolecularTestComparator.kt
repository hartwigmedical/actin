package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

public class PriorMolecularTestComparator implements Comparator<PriorMolecularTest> {

    @Override
    public int compare(@NotNull PriorMolecularTest priorMolecularTest1, @NotNull PriorMolecularTest priorMolecularTest2) {
        return priorMolecularTest1.item().compareTo(priorMolecularTest2.item());
    }
}
