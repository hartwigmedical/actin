package com.hartwig.actin.clinical.datamodel;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;

import org.jetbrains.annotations.NotNull;

public class TreatmentExamples {

    private static final EligibilityFunction IS_YOUNG_AND_FIT = eligibilityFunction(EligibilityRule.AND,
            eligibilityFunction(EligibilityRule.NOT, eligibilityFunction(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, "75")),
            eligibilityFunction(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, "1"));

    public static void main(@NotNull String... args) throws IOException {
        Drug oxaliplatin = drug("Oxaliplatin", DrugClass.PLATINUM_COMPOUND);
        Drug fluorouracil = drug("5-FU", DrugClass.PYRIMIDINE_ANTAGONIST);
        Drug irinotecan = drug("Irinotecan", DrugClass.TOPO1_INHIBITOR);

        Chemotherapy folfirinox = ImmutableChemotherapy.builder()
                .name("FOLFIRINOX")
                .isSystemic(true)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                //                .addEligibilityFunctions(IS_YOUNG_AND_FIT)
                //                .addLines(1, 2)
                .synonyms(Collections.emptySet())
                .addDrugs(oxaliplatin, fluorouracil, irinotecan)
                .maxCycles(8)
                .build();

        Radiotherapy brachytherapy = ImmutableRadiotherapy.builder().name("Brachytherapy").isSystemic(false)
                //                .addTypes(treatmentType("brachytherapy", TreatmentCategory.RADIOTHERAPY))
                .addCategories(TreatmentCategory.RADIOTHERAPY)
                //                .addEligibilityFunctions(IS_YOUNG_AND_FIT)
                //                .addLines(1)
                .synonyms(Collections.emptySet())
                .build();

        CombinedTherapy radioFolfirinox = ImmutableCombinedTherapy.builder()
                .name("FOLFIRINOX + radiotherapy")
                .addTherapies(folfirinox, brachytherapy)
                .isSystemic(true)
                //                .addLines(1, 2)
                .synonyms(Collections.emptySet())
                .build();

        Immunotherapy pembrolizumab = ImmutableImmunotherapy.builder().name("Pembrolizumab").isSystemic(true)
                //                .addTypes(treatmentType("Anti-PD-1", TreatmentCategory.IMMUNOTHERAPY))
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                //                .addEligibilityFunctions(eligibilityFunction(EligibilityRule.MSI_SIGNATURE))
                //                .addLines(1, 2)
                .synonyms(Collections.emptySet())
                .addDrugs(drug("Pembrolizumab", DrugClass.MONOCLONAL_ANTIBODY))
                .build();

        CombinedTherapy folfirinoxAndPembrolizumab = ImmutableCombinedTherapy.builder()
                .name("FOLFIRINOX + pembrolizumab")
                .addTherapies(folfirinox, pembrolizumab)
                .isSystemic(true)
                .synonyms(Collections.emptySet())
                .build();

        Chemotherapy folfirinoxLocoRegional =
                ImmutableChemotherapy.copyOf(folfirinox).withName("FOLFIRINOX loco-regional").withIsSystemic(false);

        SurgicalTreatment colectomy = ImmutableSurgicalTreatment.builder().name("Colectomy").synonyms(Collections.emptySet()).build();

        TreatmentHistoryEntry surgeryHistoryEntry =
                ImmutableTreatmentHistoryEntry.builder().addTreatments(colectomy).startYear(2021).intent(Intent.MAINTENANCE).build();

        List<TreatmentHistoryEntry> treatmentHistory = List.of(therapyHistoryEntry(Set.of(folfirinox), 2020, Intent.NEOADJUVANT),
                surgeryHistoryEntry,
                therapyHistoryEntry(Set.of(radioFolfirinox, folfirinoxLocoRegional), 2022, Intent.ADJUVANT),
                therapyHistoryEntry(Set.of(folfirinoxAndPembrolizumab), 2023, Intent.PALLIATIVE));

        for (TreatmentHistoryEntry entry : treatmentHistory) {
            System.out.printf("In %s, administered the following treatment(s) with %s intent:\n", entry.startYear(), entry.intent());
            for (Treatment treatment : entry.treatments()) {
                String categoryString = setToString(treatment.categories());
                System.out.printf(" - %s: %s, %ssystemic", treatment.name(), categoryString, treatment.isSystemic() ? "" : "non");
                if (treatment instanceof Therapy && !((Therapy) treatment).drugs().isEmpty()) {
                    System.out.printf(", with drugs %s",
                            ((Therapy) treatment).drugs()
                                    .stream()
                                    .map(drug -> String.format("%s (%s)", drug.name(), setToString(drug.drugClasses())))
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

    @NotNull
    private static Drug drug(@NotNull String name, @NotNull DrugClass drugClass) {
        return ImmutableDrug.builder().name(name).addDrugClasses(drugClass).synonyms(Collections.emptySet()).build();
    }

    @NotNull
    private static EligibilityFunction eligibilityFunction(EligibilityRule rule, Object... parameters) {
        return ImmutableEligibilityFunction.builder().rule(rule).addParameters(parameters).build();
    }

    @NotNull
    private static TreatmentHistoryEntry therapyHistoryEntry(Set<Therapy> therapies, int startYear, Intent intent) {
        return ImmutableTreatmentHistoryEntry.builder().treatments(therapies).startYear(startYear).intent(intent).build();
    }
}
