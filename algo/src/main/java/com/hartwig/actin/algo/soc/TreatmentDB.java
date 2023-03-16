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
import com.hartwig.actin.treatment.datamodel.ImmutableTreatment;
import com.hartwig.actin.treatment.datamodel.Treatment;
import com.hartwig.actin.treatment.datamodel.TreatmentComponent;

public class TreatmentDB {

    public static final String TREATMENT_CAPOX = "CAPOX";
    public static final String TREATMENT_FOLFIRI = "FOLFIRI";
    public static final String TREATMENT_FOLFIRINOX = "FOLFIRINOX";
    public static final String TREATMENT_FOLFOX = "FOLFOX";
    private static final int SCORE_CETUXIMAB_PLUS_ENCORAFENIB = 4;
    private static final int SCORE_LONSURF = 2;
    private static final int SCORE_MONOTHERAPY = 3;
    private static final int SCORE_MONOTHERAPY_PLUS_TARGETED = 4;
    private static final int SCORE_MULTITHERAPY = 5;
    private static final int SCORE_PEMBROLIZUMAB = 6;
    private static final int SCORE_TARGETED_THERAPY = 5;
    private static final String RECENT_TREATMENT_THRESHOLD_WEEKS = "104";
    private static final EligibilityFunction isColorectalCancer = ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X)
                .addParameters("colorectal cancer")
                .build();

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
                createChemotherapy(TREATMENT_CAPOX,
                        Set.of(TreatmentComponent.CAPECITABINE, TreatmentComponent.OXALIPLATIN),
                        SCORE_MULTITHERAPY),
                createChemotherapy(TREATMENT_FOLFIRI,
                        Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.IRINOTECAN),
                        SCORE_MULTITHERAPY,
                        false,
                        Set.of(1, 2),
                        Set.of(eligibleIfTreatmentNotInHistory(TREATMENT_CAPOX), eligibleIfTreatmentNotInHistory(TREATMENT_FOLFOX))),
                createChemotherapy(TREATMENT_FOLFOX,
                        Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN),
                        SCORE_MULTITHERAPY),
                createChemotherapy(TREATMENT_FOLFIRINOX,
                        Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN, TreatmentComponent.IRINOTECAN),
                        SCORE_MULTITHERAPY));
    }

    private static Treatment createChemotherapy(String name, Set<TreatmentComponent> components, int score) {
        return createChemotherapy(name, components, score, false, Set.of(1, 2), Collections.emptySet());
    }

    private static Treatment createChemotherapy(String name, Set<TreatmentComponent> components, int score, boolean isOptional, Set<Integer> lines,
            Set<EligibilityFunction> extraFunctions) {
        return ImmutableTreatment.builder()
                .name(name)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .addAllComponents(components)
                .score(score)
                .isOptional(isOptional)
                .lines(lines)
                .addEligibilityFunctions(isColorectalCancer, eligibleIfTreatmentNotInHistory(name))
                .addAllEligibilityFunctions(extraFunctions)
                .build();
    }

    private static Stream<Treatment> antiEGFRTherapies() {
        return Stream.of(createAntiEGFRTherapy("Cetuximab", TreatmentComponent.CETUXIMAB, SCORE_TARGETED_THERAPY),
                createAntiEGFRTherapy("Pantitumumab", TreatmentComponent.PANTITUMUMAB, SCORE_TARGETED_THERAPY));
    }

    private static Treatment createAntiEGFRTherapy(String name, TreatmentComponent component, int score) {
        // TODO: require left-sided tumor
        return ImmutableTreatment.builder()
                .name(name)
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .addComponents(component)
                .isOptional(false)
                .score(score)
                .lines(Set.of(2, 3))
                .addEligibilityFunctions(isColorectalCancer, eligibleIfGenesAreWildType(Stream.of("KRAS", "NRAS", "BRAF")))
                .build();

    }

    private static Stream<Treatment> otherTreatments() {
        return Stream.of(createChemotherapy("Lonsurf",
                        Set.of(TreatmentComponent.TRIFLURIDINE, TreatmentComponent.TIPIRACIL),
                        SCORE_LONSURF,
                        true,
                        Set.of(3),
                        Collections.emptySet()),
                ImmutableTreatment.builder()
                        .name("Pembrolizumab")
                        .addComponents(TreatmentComponent.PEMBROLIZUMAB)
                        .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                        .isOptional(false)
                        .score(SCORE_PEMBROLIZUMAB)
                        .lines(Set.of(1, 2))
                        .addEligibilityFunctions(isColorectalCancer,
                                ImmutableEligibilityFunction.builder().rule(EligibilityRule.MSI_SIGNATURE).build())
                        .build(),
                ImmutableTreatment.builder()
                        .name("Cetuximab + Encorafenib")
                        .addComponents(TreatmentComponent.CETUXIMAB, TreatmentComponent.ENCORAFENIB)
                        .addCategories(TreatmentCategory.TARGETED_THERAPY)
                        .isOptional(false)
                        .score(SCORE_CETUXIMAB_PLUS_ENCORAFENIB)
                        .lines(Set.of(2, 3))
                        .addEligibilityFunctions(isColorectalCancer,
                                ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X)
                                        .addParameters("BRAF")
                                        .build())
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
        return ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.AND)
                .addAllParameters(genes.map(gene -> ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.WILDTYPE_OF_GENE_X)
                        .addParameters(gene)
                        .build()).collect(Collectors.toSet()))
                .build();
    }

    private static EligibilityFunction eligibleIfTreatmentNotInHistory(String treatmentName) {
        return ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.NOT)
                .addParameters(ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.OR)
                        .addParameters(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS)
                                        .addParameters(treatmentName, RECENT_TREATMENT_THRESHOLD_WEEKS)
                                        .build(),
                                ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT)
                                        .addParameters(treatmentName)
                                        .build())
                        .build())
                .build();
    }
}
