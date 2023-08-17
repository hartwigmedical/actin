package com.hartwig.actin.clinical.datamodel;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.clinical.datamodel.treatment.Therapy;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;

import org.jetbrains.annotations.NotNull;

public class TestTreatmentExamples {

    public static void main(@NotNull String... args) throws IOException {
        for (TreatmentHistoryEntry entry : TestClinicalFactory.createProperTestClinicalRecord().treatmentHistory()) {
            System.out.printf("In %s, administered the following treatment(s) with %s intent:\n", entry.startYear(), entry.intents());
            for (Treatment treatment : entry.treatments()) {
                String categoryString = setToString(treatment.categories());
                System.out.printf(" - %s: %s, %ssystemic", treatment.name(), categoryString, treatment.isSystemic() ? "" : "non");
                if (treatment instanceof Therapy && !((Therapy) treatment).drugs().isEmpty()) {
                    System.out.printf(", with drugs %s",
                            ((Therapy) treatment).drugs()
                                    .stream()
                                    .map(drug -> String.format("%s (%s)", drug.name(), setToString(drug.drugTypes())))
                                    .collect(Collectors.joining(", ")));
                }
                System.out.println();
            }
        }
    }

    @NotNull
    private static <T> String setToString(@NotNull Set<T> categories) {
        return categories.stream().map(Objects::toString).collect(Collectors.joining("/"));
    }
}
