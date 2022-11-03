package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GeneAlteration {

    @NotNull
    String gene();

    @NotNull
    GeneRole geneRole();

    @NotNull
    ProteinEffect proteinEffect();

    @Nullable
    Boolean associatedWithDrugResistance();
}
