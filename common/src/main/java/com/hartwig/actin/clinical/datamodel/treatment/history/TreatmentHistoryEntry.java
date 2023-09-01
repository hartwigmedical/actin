package com.hartwig.actin.clinical.datamodel.treatment.history;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentHistoryEntry {
    private static final String DELIMITER = ";";

    @NotNull
    public abstract Set<Treatment> treatments();

    @Nullable
    public abstract Integer startYear();

    @Nullable
    public abstract Integer startMonth();

    @Nullable
    public abstract Set<Intent> intents();

    @NotNull
    @Value.Default
    public Boolean isTrial() {
        return false;
    }

    @Nullable
    public abstract String trialAcronym();

    @Nullable
    public abstract TherapyHistoryDetails therapyHistoryDetails();

    @NotNull
    public String treatmentName() {
        return treatmentStringUsingFunction(Treatment::name);
    }

    @NotNull
    public String treatmentDisplay() {
        return treatmentStringUsingFunction(Treatment::display);
    }

    @NotNull
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
        return treatments().stream().noneMatch(treatment -> treatment.types().isEmpty());
    }

    private boolean isTypeFromCollection(Set<TreatmentType> types) {
        return treatments().stream().flatMap(treatment -> treatment.types().stream()).anyMatch(types::contains);
    }

    @NotNull
    private String treatmentStringUsingFunction(Function<Treatment, String> treatmentField) {
        String nameString = treatments().stream().map(treatmentField).collect(Collectors.joining(DELIMITER));
        return !nameString.isEmpty() ? nameString : treatmentCategoryDisplay();
    }

    @NotNull
    private String treatmentCategoryDisplay() {
        return treatments().stream()
                .flatMap(t -> t.categories().stream().map(TreatmentCategory::display))
                .collect(Collectors.joining(DELIMITER));
    }
}
