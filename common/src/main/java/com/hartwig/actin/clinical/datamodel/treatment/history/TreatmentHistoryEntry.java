package com.hartwig.actin.clinical.datamodel.treatment.history;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentHistoryEntry {

    @NotNull
    public abstract Set<Treatment> treatments();

    @Nullable
    public abstract Integer startYear();

    @Nullable
    public abstract Integer startMonth();

    @Nullable
    public abstract Set<Intent> intents();

    @Nullable
    public abstract Boolean isTrial();

    @Nullable
    public abstract String trialAcronym();

    @Nullable
    public abstract TherapyHistoryDetails therapyHistoryDetails();

    @NotNull
    public String treatmentName() {
        String nameString = treatments().stream().map(Treatment::name).collect(Collectors.joining(";"));
        return !nameString.isEmpty()
                ? nameString
                : treatments().stream()
                        .flatMap(t -> t.categories().stream().map(TreatmentCategory::display))
                        .collect(Collectors.joining(";"));
    }

    public Set<TreatmentCategory> categories() {
        return treatments().stream().flatMap(treatment -> treatment.categories().stream()).collect(Collectors.toSet());
    }

    @Nullable
    public Boolean isOfType(TreatmentType typeToFind) {
        return matchesTypeFromSet(Set.of(typeToFind));
    }

    @Nullable
    public Boolean matchesTypeFromSet(Set<TreatmentType> types) {
        return hasTypeConfigured() ? isTypeFromCollection(types) : null;
    }

    public boolean hasTypeConfigured() {
        return treatments().stream().noneMatch(treatment -> treatment.types() == null);
    }

    private boolean isTypeFromCollection(Set<TreatmentType> types) {
        return treatments().stream()
                .flatMap(treatment -> treatment.types() != null ? treatment.types().stream() : Stream.empty())
                .anyMatch(types::contains);
    }
}
