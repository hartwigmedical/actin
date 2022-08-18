package com.hartwig.actin.algo.molecular;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public final class MolecularInterpretation {

    private MolecularInterpretation() {
    }

    public static boolean hasGeneAmplified(@NotNull MolecularRecord molecular, @NotNull String gene) {
        for (Amplification amplification : molecular.drivers().amplifications()) {
            if (amplification.gene().equals(gene)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasGeneInactivated(@NotNull MolecularRecord molecular, @NotNull String gene) {
        for (Loss loss : molecular.drivers().losses()) {
            if (loss.gene().equals(gene)) {
                return true;
            }
        }

        for (HomozygousDisruption homozygousDisruption : molecular.drivers().homozygousDisruptions()) {
            if (homozygousDisruption.gene().equals(gene)) {
                return true;
            }
        }

        for (Variant variant : molecular.drivers().variants()) {
            if (variant.gene().equals(gene) && variant.driverLikelihood() == DriverLikelihood.HIGH) {
                return true;
            }
        }

        return false;
    }
}
