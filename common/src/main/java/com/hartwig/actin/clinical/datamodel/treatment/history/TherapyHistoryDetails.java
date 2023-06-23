package com.hartwig.actin.clinical.datamodel.treatment.history;

import java.time.LocalDate;
import java.util.Set;

import com.hartwig.actin.clinical.datamodel.BodyLocationCategory;
import com.hartwig.actin.clinical.datamodel.ObservedToxicity;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface TherapyHistoryDetails {

    @Nullable
    Integer stopYear();

    @Nullable
    Integer stopMonth();

    @Nullable
    LocalDate ongoingAsOf();

    @Nullable
    Integer cycles();

    @Nullable
    TreatmentResponse bestResponse();

    @Nullable
    StopReason stopReason();

    @Nullable
    String stopReasonDetail();

    @Nullable
    Set<ObservedToxicity> toxicities();

    @Nullable
    Set<BodyLocationCategory> bodyLocationCategories();

    @Nullable
    Set<String> bodyLocations();
}
