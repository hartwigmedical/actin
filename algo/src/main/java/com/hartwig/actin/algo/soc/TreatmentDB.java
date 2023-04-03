package com.hartwig.actin.algo.soc;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.algo.soc.datamodel.ImmutableTreatment;
import com.hartwig.actin.algo.soc.datamodel.Treatment;
import com.hartwig.actin.algo.soc.datamodel.TreatmentComponent;

public class TreatmentDB {

    public static final String TREATMENT_CAPOX = "CAPOX";
    public static final String TREATMENT_CETUXIMAB = "Cetuximab";
    public static final String TREATMENT_FOLFIRI = "FOLFIRI";
    public static final String TREATMENT_FOLFIRINOX = "FOLFIRINOX";
    public static final String TREATMENT_FOLFOX = "FOLFOX";
    public static final String TREATMENT_LONSURF = "Lonsurf";
    public static final String TREATMENT_PANITUMUMAB = "Panitumumab";
    public static final String TREATMENT_PEMBROLIZUMAB = "Pembrolizumab";

    private static final int SCORE_CETUXIMAB_PLUS_ENCORAFENIB = 4;
    private static final int SCORE_LONSURF = 2;
    private static final int SCORE_MONOTHERAPY = 3;
    private static final int SCORE_MONOTHERAPY_PLUS_TARGETED = 4;
    private static final int SCORE_MULTITHERAPY = 5;
    private static final int SCORE_PEMBROLIZUMAB = 6;
    private static final int SCORE_TARGETED_THERAPY = 5;
    private static final String CHEMO_MAX_CYCLES = "12";
    private static final String RECENT_TREATMENT_THRESHOLD_WEEKS = "104";
    private static final EligibilityFunction IS_COLORECTAL_CANCER =
            eligibilityFunction(EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X, "colorectal cancer");

    private static final EligibilityFunction IS_YOUNG_AND_FIT = eligibilityFunction(EligibilityRule.AND,
            eligibilityFunction(EligibilityRule.NOT, eligibilityFunction(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, "75")),
            eligibilityFunction(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, "1"));

    public static Stream<Treatment> loadTreatments() {
        return Stream.of(combinableChemotherapies(),
                combinableChemotherapies().map(TreatmentDB::addBevacizumabToTreatment),
                antiEGFRTherapies(),
                antiEGFRTherapies().flatMap(therapy -> combinableChemotherapies().map(chemo -> addComponentsToTreatment(therapy,
                        chemo.name(),
                        chemo.components(),
                        TreatmentCategory.CHEMOTHERAPY,
                        SCORE_MONOTHERAPY_PLUS_TARGETED,
                        Set.of(2)))),
                otherTreatments()).flatMap(Function.identity());
    }

    private static Stream<Treatment> combinableChemotherapies() {
        return Stream.of(createChemotherapy("5-FU", Set.of(TreatmentComponent.FLUOROURACIL), SCORE_MONOTHERAPY),
                createChemotherapy("Capecitabine", Set.of(TreatmentComponent.CAPECITABINE), SCORE_MONOTHERAPY),
                createChemotherapy("Irinotecan", Set.of(TreatmentComponent.IRINOTECAN), SCORE_MONOTHERAPY),
                createChemotherapy("Oxaliplatin", Set.of(TreatmentComponent.OXALIPLATIN), -1),
                createMultiChemotherapy(TREATMENT_CAPOX, Set.of(TreatmentComponent.CAPECITABINE, TreatmentComponent.OXALIPLATIN)),
                createChemotherapy(TREATMENT_FOLFIRI,
                        Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.IRINOTECAN),
                        SCORE_MULTITHERAPY,
                        false,
                        Set.of(1, 2),
                        Set.of(eligibleIfTreatmentNotInHistory(TREATMENT_CAPOX), eligibleIfTreatmentNotInHistory(TREATMENT_FOLFOX),
                                IS_YOUNG_AND_FIT)),
                createMultiChemotherapy(TREATMENT_FOLFOX, Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN)),
                createMultiChemotherapy(TREATMENT_FOLFIRINOX,
                        Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN, TreatmentComponent.IRINOTECAN)));
    }

    private static Treatment createChemotherapy(String name, Set<TreatmentComponent> components, int score) {
        return createChemotherapy(name, components, score, false, Set.of(1, 2), Collections.emptySet());
    }

    private static Treatment createMultiChemotherapy(String name, Set<TreatmentComponent> components) {
        return createChemotherapy(name, components, SCORE_MULTITHERAPY, false, Set.of(1, 2), Set.of(IS_YOUNG_AND_FIT));
    }

    private static Treatment createChemotherapy(String name, Set<TreatmentComponent> components, int score, boolean isOptional,
            Set<Integer> lines, Set<EligibilityFunction> extraFunctions) {
        return ImmutableTreatment.builder()
                .name(name)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .addAllComponents(components)
                .score(score)
                .isOptional(isOptional)
                .lines(lines)
                .addEligibilityFunctions(IS_COLORECTAL_CANCER, eligibleIfTreatmentNotInHistory(name))
                .addAllEligibilityFunctions(extraFunctions)
                .build();
    }

    private static Stream<Treatment> antiEGFRTherapies() {
        return Stream.of(createAntiEGFRTherapy(TREATMENT_CETUXIMAB, TreatmentComponent.CETUXIMAB),
                createAntiEGFRTherapy(TREATMENT_PANITUMUMAB, TreatmentComponent.PANITUMUMAB));
    }

    private static Treatment createAntiEGFRTherapy(String name, TreatmentComponent component) {
        return ImmutableTreatment.builder()
                .name(name)
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .addComponents(component)
                .isOptional(false)
                .score(TreatmentDB.SCORE_TARGETED_THERAPY)
                .lines(Set.of(2, 3))
                .addEligibilityFunctions(IS_COLORECTAL_CANCER,
                        eligibleIfGenesAreWildType(Stream.of("KRAS", "NRAS", "BRAF")),
                        eligibilityFunction(EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR))
                .build();

    }

    private static Stream<Treatment> otherTreatments() {
        return Stream.of(createChemotherapy(TREATMENT_LONSURF,
                        Set.of(TreatmentComponent.TRIFLURIDINE, TreatmentComponent.TIPIRACIL),
                        SCORE_LONSURF,
                        true,
                        Set.of(3),
                        Set.of(eligibleIfTreatmentNotInHistory("trifluridine"))),
                ImmutableTreatment.builder()
                        .name(TREATMENT_PEMBROLIZUMAB)
                        .addComponents(TreatmentComponent.PEMBROLIZUMAB)
                        .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                        .isOptional(false)
                        .score(SCORE_PEMBROLIZUMAB)
                        .lines(Set.of(1, 2))
                        .addEligibilityFunctions(IS_COLORECTAL_CANCER, eligibilityFunction(EligibilityRule.MSI_SIGNATURE))
                        .build(),
                ImmutableTreatment.builder()
                        .name("Cetuximab + Encorafenib")
                        .addComponents(TreatmentComponent.CETUXIMAB, TreatmentComponent.ENCORAFENIB)
                        .addCategories(TreatmentCategory.TARGETED_THERAPY)
                        .isOptional(false)
                        .score(SCORE_CETUXIMAB_PLUS_ENCORAFENIB)
                        .lines(Set.of(2, 3))
                        .addEligibilityFunctions(IS_COLORECTAL_CANCER,
                                eligibilityFunction(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, "BRAF"))
                        .build());
    }

    private static Treatment addBevacizumabToTreatment(Treatment treatment) {
        return addComponentsToTreatment(treatment,
                "Bevacizumab",
                Set.of(TreatmentComponent.BEVACIZUMAB),
                TreatmentCategory.IMMUNOTHERAPY,
                Math.max(treatment.score(), SCORE_MONOTHERAPY_PLUS_TARGETED),
                treatment.lines());
    }

    private static Treatment addComponentsToTreatment(Treatment treatment, String name, Set<TreatmentComponent> components,
            TreatmentCategory category, int score, Set<Integer> lines) {
        return ImmutableTreatment.builder()
                .from(treatment)
                .name(treatment.name() + " + " + name)
                .addAllComponents(components)
                .addCategories(category)
                .score(score)
                .lines(lines)
                .build();
    }

    private static EligibilityFunction eligibleIfGenesAreWildType(Stream<String> genes) {
        return eligibilityFunction(EligibilityRule.AND,
                genes.map(gene -> eligibilityFunction(EligibilityRule.WILDTYPE_OF_GENE_X, gene))
                        .collect(Collectors.toList())
                        .toArray(Object[]::new));
    }

    private static EligibilityFunction eligibleIfTreatmentNotInHistory(String treatmentName) {
        return eligibilityFunction(EligibilityRule.NOT,
                eligibilityFunction(EligibilityRule.OR,
                        eligibilityFunction(EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS,
                                treatmentName,
                                RECENT_TREATMENT_THRESHOLD_WEEKS),
                        eligibilityFunction(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT, treatmentName),
                        eligibilityFunction(EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES,
                                treatmentName, CHEMO_MAX_CYCLES, CHEMO_MAX_CYCLES)));
    }

    private static EligibilityFunction eligibilityFunction(EligibilityRule rule, Object... parameters) {
        return ImmutableEligibilityFunction.builder().rule(rule).addParameters(parameters).build();
    }
}
