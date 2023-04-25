package com.hartwig.actin.algo.calendar;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;

import org.jetbrains.annotations.NotNull;

public final class ReferenceDateProviderFactory {

    private ReferenceDateProviderFactory() {
    }

    @NotNull
    public static ReferenceDateProvider create(@NotNull ClinicalRecord clinical, boolean runHistorically) {
        return runHistorically ? HistoricDateProvider.fromClinical(clinical) : new CurrentDateProvider();
    }
}
