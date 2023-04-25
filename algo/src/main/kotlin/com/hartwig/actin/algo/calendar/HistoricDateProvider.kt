package com.hartwig.actin.algo.calendar;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;

import org.jetbrains.annotations.NotNull;

class HistoricDateProvider implements ReferenceDateProvider {

    @NotNull
    private final LocalDate historicDate;

    @NotNull
    public static HistoricDateProvider fromClinical(@NotNull ClinicalRecord clinical) {
        LocalDate historicDate = clinical.patient().registrationDate().plusWeeks(3);
        LocalDate currentDate = LocalDate.now();
        LocalDate effectiveDate = currentDate.isBefore(historicDate) ? currentDate : historicDate;

        return new HistoricDateProvider(effectiveDate);
    }

    private HistoricDateProvider(@NotNull final LocalDate historicDate) {
        this.historicDate = historicDate;
    }

    @NotNull
    @Override
    public LocalDate date() {
        return historicDate;
    }

    @Override
    public boolean isLive() {
        return false;
    }
}
