package com.hartwig.actin.algo.soc;

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

    private static final int SCORE_MONOTHERAPY = 3;
    private static final int SCORE_MONOTHERAPY_PLUS_TARGETED = 4;
    private static final int SCORE_MULTITHERAPY = 5;
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
        return Stream.of(createChemotherapyWithNameAndComponents("5-FU", Set.of(TreatmentComponent.FLUOROURACIL), SCORE_MONOTHERAPY),
                createChemotherapyWithNameAndComponents("Capecitabine", Set.of(TreatmentComponent.CAPECITABINE), SCORE_MONOTHERAPY),
                createChemotherapyWithNameAndComponents("Irinotecan", Set.of(TreatmentComponent.IRINOTECAN), SCORE_MONOTHERAPY),
                createChemotherapyWithNameAndComponents("Oxaliplatin", Set.of(TreatmentComponent.OXALIPLATIN), -1),
                createChemotherapyWithNameAndComponents("CAPOX", Set.of(TreatmentComponent.CAPECITABINE, TreatmentComponent.OXALIPLATIN),
                        SCORE_MULTITHERAPY),
                createChemotherapyWithNameAndComponents("FOLFIRI", Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.IRINOTECAN),
                        SCORE_MULTITHERAPY),
                createChemotherapyWithNameAndComponents("FOLFOX", Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN),
                        SCORE_MULTITHERAPY),
                createChemotherapyWithNameAndComponents("FOLFIRINOX", Set.of(TreatmentComponent.FLUOROURACIL, TreatmentComponent.OXALIPLATIN,
                        TreatmentComponent.IRINOTECAN), SCORE_MULTITHERAPY)
                ).flatMap(treatment -> Stream.of(treatment, addBevacizumabToTreatment(treatment)));
    }

    private Treatment createChemotherapyWithNameAndComponents(String name, Set<TreatmentComponent> components, int score) {
        return ImmutableTreatment.builder()
                .name(name)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .addAllComponents(components)
                .isOptional(false)
                .score(score)
                .addEligibilityFunctions(isColorectalCancer)
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
}
