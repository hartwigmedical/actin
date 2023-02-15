package com.hartwig.actin.algo.evaluation.complication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComplicationUtil {
    private final PatientRecord record;

    public ComplicationUtil(@NotNull PatientRecord record) {
        this.record = record;
    }

    public Optional<List<Complication>> complicationsMatchingCategories(List<String> categorySearchTerms) {
        return Optional.ofNullable(record.clinical().complications())
                .map(complicationList -> complicationList.stream()
                        .map(complication -> ImmutableComplication.builder().from(complication).categories(
                                complication.categories().stream().filter(category ->
                                        categorySearchTerms.stream().anyMatch(term ->
                                                category.toLowerCase().contains(term.toLowerCase())
                                        )
                                ).collect(Collectors.toList())).build()
                        ).filter(complication -> !complication.categories().isEmpty()).collect(Collectors.toList())
                );
    }
}
