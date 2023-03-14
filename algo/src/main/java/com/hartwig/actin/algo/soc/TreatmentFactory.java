package com.hartwig.actin.algo.soc;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.datamodel.ImmutableTreatment;
import com.hartwig.actin.treatment.datamodel.Treatment;
import com.hartwig.actin.treatment.datamodel.TreatmentComponent;

public class TreatmentFactory {

    public static final String TREATMENT_CAPOX = "CAPOX";
    public static final String TREATMENT_FOLFIRI = "FOLFIRI";
    public static final String TREATMENT_FOLFIRINOX = "FOLFIRINOX";
    public static final String TREATMENT_FOLFOX = "FOLFOX";
    private static final int SCORE_MONOTHERAPY = 3;
    private static final int SCORE_MONOTHERAPY_PLUS_TARGETED = 4;
    private static final int SCORE_MULTITHERAPY = 5;
    private static final String RECENT_TREATMENT_THRESHOLD_WEEKS = "104";
    private final EligibilityFunction isColorectalCancer;
    private final DoidModel doidModel;

    public TreatmentFactory(DoidModel doidModel) {
        this.doidModel = doidModel;
        isColorectalCancer = ImmutableEligibilityFunction.builder()
                .rule(EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X)
                .addParameters("colorectal cancer")
                .build();
    }

    public Stream<Treatment> loadTreatments() {
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
                        Set.of(eligibleIfTreatmentNotInHistory(TREATMENT_CAPOX), eligibleIfTreatmentNotInHistory(TREATMENT_FOLFOX))),
                createChemotherapy(TREATMENT_FOLFOX,
                        Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN),
                        SCORE_MULTITHERAPY),
                createChemotherapy(TREATMENT_FOLFIRINOX,
                        Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN, TreatmentComponent.IRINOTECAN),
                        SCORE_MULTITHERAPY)).flatMap(treatment -> Stream.of(treatment, addBevacizumabToTreatment(treatment)));
    }

    private Treatment createChemotherapy(String name, Set<TreatmentComponent> components, int score) {
        return createChemotherapy(name, components, score, Collections.emptySet());
    }

    private Treatment createChemotherapy(String name, Set<TreatmentComponent> components, int score,
            Set<EligibilityFunction> extraFunctions) {
        return ImmutableTreatment.builder()
                .name(name)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .addAllComponents(components)
                .isOptional(false)
                .score(score)
                .addEligibilityFunctions(isColorectalCancer, eligibleIfTreatmentNotInHistory(name))
                .addAllEligibilityFunctions(extraFunctions)
                .build();
    }

    private Treatment addBevacizumabToTreatment(Treatment treatment) {
        return ImmutableTreatment.builder()
                .from(treatment)
                .name(treatment.name() + " + Bevacizumab")
                .addComponents(TreatmentComponent.BEVACIZUMAB)
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                .score(Math.max(treatment.score(), SCORE_MONOTHERAPY_PLUS_TARGETED))
                .build();
    }

    private EligibilityFunction eligibleIfTreatmentNotInHistory(String treatmentName) {
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
