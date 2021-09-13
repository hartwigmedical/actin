package com.hartwig.actin.clinical;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

public final class ClinicalModelFactory {

    private ClinicalModelFactory() {
    }

    @NotNull
    public static ClinicalModel buildFromClinicalDataDump() {
        // TODO implement
        return new ClinicalModel(Lists.newArrayList());
    }
}
