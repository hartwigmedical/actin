package com.hartwig.actin.algo.calendar;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;

import org.jetbrains.annotations.NotNull;

class HistoricDateProvider implements ReferenceDateProvider {

    @NotNull
    private final LocalDate historicDate;

    @NotNull
    public static HistoricDateProvider fromClinical(@NotNull ClinicalRecord clinical) {
        return new HistoricDateProvider(clinical.patient().registrationDate().plusWeeks(3));
    }

    private HistoricDateProvider(@NotNull final LocalDate historicDate) {
        this.historicDate = historicDate;
    }

    @NotNull
    @Override
    public LocalDate date() {
        return null;
    }
}
