package com.hartwig.actin.clinical.datamodel.treatment.history;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
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
    public abstract TreatmentHistoryDetails treatmentHistoryDetails();

    @NotNull
    public Set<Treatment> allTreatments() {
        TreatmentHistoryDetails details = treatmentHistoryDetails();
        if (details == null) {
            return treatments();
        }
        Stream<Treatment> maintenanceTreatmentStream =
                details.maintenanceTreatment() == null ? Stream.empty() : Stream.of(details.maintenanceTreatment().treatment());

        Stream<Treatment> switchToTreatmentStream = details.switchToTreatments() == null
                ? Stream.empty()
                : details.switchToTreatments().stream().map(TreatmentStage::treatment);

        return Stream.of(treatments().stream(), maintenanceTreatmentStream, switchToTreatmentStream)
                .flatMap(Function.identity())
                .collect(Collectors.toSet());
    }

    @NotNull
    public String treatmentName() {
        return treatmentStringUsingFunction(allTreatments(), Treatment::name);
    }

    @NotNull
    public Set<TreatmentCategory> categories() {
        return allTreatments().stream().flatMap(treatment -> treatment.categories().stream()).collect(Collectors.toSet());
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
        return allTreatments().stream().noneMatch(treatment -> treatment.types().isEmpty());
    }

    private boolean isTypeFromCollection(Set<TreatmentType> types) {
        return allTreatments().stream().flatMap(treatment -> treatment.types().stream()).anyMatch(types::contains);
    }

    @NotNull
    public String treatmentDisplay() {
        Set<String> treatmentNames = treatments().stream().map(Treatment::display).map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> chemoradiationTherapyNames = Set.of("chemotherapy", "radiotherapy");

        if (treatmentNames.containsAll(chemoradiationTherapyNames)) {
            List<Treatment> remainingTreatments = treatments().stream()
                    .filter(treatment -> !chemoradiationTherapyNames.contains(treatment.display().toLowerCase()))
                    .collect(Collectors.toList());

            if (remainingTreatments.isEmpty()) {
                return "Chemoradiation";
            } else if (remainingTreatments.size() == 1) {
                Treatment remainingTreatment = remainingTreatments.get(0);
                if (remainingTreatment.categories().contains(TreatmentCategory.CHEMOTHERAPY)) {
                    return String.format("Chemoradiation (with %s)", remainingTreatment.display());
                } else {
                    return String.format("Chemoradiation and %s", remainingTreatment.display());
                }
            }
        }
        return treatmentStringUsingFunction(treatments(), Treatment::display);
    }

    @NotNull
    private static String treatmentStringUsingFunction(Set<Treatment> treatments, Function<Treatment, String> treatmentField) {
        String nameString = treatments.stream().map(treatmentField).distinct().collect(Collectors.joining(DELIMITER));
        return !nameString.isEmpty() ? nameString : treatmentCategoryDisplay(treatments);
    }

    @NotNull
    private static String treatmentCategoryDisplay(Set<Treatment> treatments) {
        return treatments.stream().flatMap(t -> t.categories().stream().map(TreatmentCategory::display)).distinct()
                .collect(Collectors.joining(DELIMITER));
    }
}
