package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import org.jetbrains.annotations.NotNull;

public final class PatternMatcher {

    private PatternMatcher() {
    }

    public static boolean isMatch(@NotNull String term, @NotNull Set<List<String>> patterns) {
        String termToEvaluate = term.toLowerCase();
        for (List<String> pattern : patterns) {
            boolean patternMatch = true;
            int prevIndexOf = -1;
            for (String item : pattern) {
                int curIndexOf = termToEvaluate.indexOf(item);
                if (curIndexOf <= prevIndexOf) {
                    patternMatch = false;
                }
                prevIndexOf = curIndexOf;
            }

            if (patternMatch) {
                return true;
            }
        }

        return false;
    }

    public static Set<String> findComplicationNamesMatchingCategories(PatientRecord record,
                                                                      List<String> categorySearchTerms) {
        return findComplicationsMatchingCategories(record, categorySearchTerms).orElse(Stream.empty())
                .map(Complication::name)
                .collect(Collectors.toSet());
    }

    public static Set<String> findComplicationCategoriesMatchingCategories(PatientRecord record,
                                                                            List<String> categorySearchTerms) {
        return findComplicationsMatchingCategories(record, categorySearchTerms).orElse(Stream.empty())
                .flatMap(complication -> complication.categories().stream())
                .collect(Collectors.toSet());
    }

    private static Optional<Stream<ImmutableComplication>> findComplicationsMatchingCategories(PatientRecord record,
                                                                                      List<String> categorySearchTerms) {
        return Optional.ofNullable(record.clinical().complications())
                .map(complicationList -> complicationList.stream().map(complication ->
                                ImmutableComplication.builder()
                                        .from(complication)
                                        .categories(
                                                complication.categories().stream().filter(category ->
                                                        categorySearchTerms.stream().anyMatch(term ->
                                                                category.toLowerCase().contains(term.toLowerCase())
                                                        )
                                                ).collect(Collectors.toList())
                                        )
                                        .build()
                    )
                    .filter(complication -> !complication.categories().isEmpty())
                );
    }
}
