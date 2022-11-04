package com.hartwig.actin.report.interpretation;

import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class MolecularEventFactory {

    private static final Logger LOGGER = LogManager.getLogger(MolecularEventFactory.class);

    public static final String MICROSATELLITE_UNSTABLE = "MSI";
    public static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HRD";
    public static final String HIGH_TUMOR_MUTATIONAL_BURDEN = "TMB High";
    public static final String HIGH_TUMOR_MUTATIONAL_LOAD = "TML High";

    private MolecularEventFactory() {
    }

    @NotNull
    public static String variantEvent(@NotNull Variant variant) {
        return variant.gene() + " " + variant.canonicalImpact().proteinImpact();
    }

}
