package com.hartwig.actin.report.interpretation;

import java.util.Comparator;

import com.hartwig.actin.molecular.sort.driver.DriverLikelihoodComparator;

import org.jetbrains.annotations.NotNull;

public class MolecularDriverEntryComparator implements Comparator<MolecularDriverEntry> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    @Override
    public int compare(@NotNull MolecularDriverEntry molecularDriverEntry1, @NotNull MolecularDriverEntry molecularDriverEntry2) {
        int driverCompare =
                DRIVER_LIKELIHOOD_COMPARATOR.compare(molecularDriverEntry1.driverLikelihood(), molecularDriverEntry2.driverLikelihood());

        if (driverCompare != 0) {
            return driverCompare;
        }

        int driverTypeCompare = molecularDriverEntry1.driverType().compareTo(molecularDriverEntry2.driverType());
        if (driverTypeCompare != 0) {
            return driverTypeCompare;
        }

        return molecularDriverEntry1.driver().compareTo(molecularDriverEntry2.driver());
    }
}
