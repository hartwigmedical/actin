package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;

public final class ComplicationFunctions {

    public static Set<String> findComplicationNamesMatchingAnyCategory(PatientRecord record, List<String> categorySearchTerms) {
        return findComplicationsMatchingAnyCategory(record, categorySearchTerms).orElse(Stream.empty())
                .map(Complication::name)
                .collect(Collectors.toSet());
    }

    public static Set<String> findComplicationCategoriesMatchingAnyCategory(PatientRecord record, List<String> categorySearchTerms) {
        return findComplicationsMatchingAnyCategory(record, categorySearchTerms).orElse(Stream.empty())
                .flatMap(complication -> complication.categories().stream())
                .collect(Collectors.toSet());
    }

    private static Optional<Stream<ImmutableComplication>> findComplicationsMatchingAnyCategory(PatientRecord record,
            List<String> categorySearchTerms) {
        return Optional.ofNullable(record.clinical().complications())
                .map(complicationList -> complicationList.stream()
                        .map(complication -> ImmutableComplication.builder()
                                .from(complication)
                                .categories(complication.categories()
                                        .stream()
                                        .filter(category -> categorySearchTerms.stream()
                                                .anyMatch(term -> category.toLowerCase().contains(term.toLowerCase())))
                                        .collect(Collectors.toList()))
                                .build())
                        .filter(complication -> !complication.categories().isEmpty()));
    }
}
