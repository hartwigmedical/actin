package com.hartwig.actin.clinical.datamodel;

import java.time.Year;
import java.time.YearMonth;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TreatmentHistoryEntry {

    @NotNull
    Set<Treatment> treatments();

    @Nullable
    Year startYear();

    @Nullable
    YearMonth startMonth();

    @Nullable
    Intent intent();

    @Nullable
    Boolean isTrial();

    @Nullable
    String trialAcronym();
}
