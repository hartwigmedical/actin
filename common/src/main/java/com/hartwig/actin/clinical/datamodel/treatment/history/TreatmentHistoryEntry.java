package com.hartwig.actin.clinical.datamodel.treatment.history;

import java.util.Set;

import com.hartwig.actin.clinical.datamodel.BodyLocationCategory;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface TreatmentHistoryEntry {

    @NotNull
    Set<Treatment> treatments();

    @NotNull
    String rawInput();

    @Nullable
    Integer startYear();

    @Nullable
    Integer startMonth();

    @Nullable
    Set<Intent> intents();

    @Nullable
    Boolean isTrial();

    @Nullable
    String trialAcronym();

    @Nullable
    TherapyHistoryDetails therapyHistoryDetails();

    @Nullable
    SurgeryHistoryDetails surgeryHistoryDetails();

    @Nullable
    Set<BodyLocationCategory> bodyLocationCategories();

    @Nullable
    Set<String> bodyLocations();
}
