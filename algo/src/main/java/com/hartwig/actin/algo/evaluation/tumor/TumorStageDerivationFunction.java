package com.hartwig.actin.algo.evaluation.tumor;

import static java.util.function.Predicate.not;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.Nullable;

class TumorStageDerivationFunction {

    private final Map<Predicate<TumorDetails>, Set<TumorStage>> derivationRules;

    TumorStageDerivationFunction(final DoidModel doidModel) {
        this.derivationRules = Map.of(not(hasAtLeastCategorizedLesions(1, doidModel)).and(not(hasAnyUncategorizedLesions())),
                Set.of(TumorStage.I, TumorStage.II),
                hasLymphNodeLesions(doidModel).or(hasAtLeastCategorizedLesions(1, doidModel)),
                Set.of(TumorStage.III, TumorStage.IV),
                hasAtLeastCategorizedLesions(2, doidModel),
                Set.of(TumorStage.IV));
    }

    Set<TumorStage> apply(final TumorDetails tumor) {
        return Optional.ofNullable(tumor.stage())
                .map(s -> Collections.<TumorStage>emptySet())
                .orElse(DoidEvaluationFunctions.hasConfiguredDoids(tumor.doids()) ? derivationRules.entrySet()
                        .stream()
                        .filter(rule -> rule.getKey().test(tumor))
                        .map(Map.Entry::getValue)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet()) : Collections.emptySet());
    }

    private static Predicate<TumorDetails> hasLymphNodeLesions(final DoidModel doidModel) {
        return tumorDetails -> evaluateMetastases(tumorDetails.hasLymphNodeLesions(),
                tumorDetails.doids(),
                DoidConstants.LYMPH_NODE_CANCER_DOID,
                doidModel);
    }

    private static Predicate<TumorDetails> hasAtLeastCategorizedLesions(final int min, final DoidModel doidModel) {
        return tumor -> Stream.of(evaluateMetastases(tumor.hasLiverLesions(), tumor.doids(), DoidConstants.LIVER_CANCER_DOID, doidModel),
                evaluateMetastases(tumor.hasCnsLesions(), tumor.doids(), DoidConstants.CNS_CANCER_DOID, doidModel),
                evaluateMetastases(tumor.hasBrainLesions(), tumor.doids(), DoidConstants.BRAIN_CANCER_DOID, doidModel),
                evaluateMetastases(tumor.hasLungLesions(), tumor.doids(), DoidConstants.LUNG_CANCER_DOID, doidModel),
                evaluateMetastases(tumor.hasBoneLesions(), tumor.doids(), DoidConstants.BONE_CANCER_DOID, doidModel)).filter(b -> b).count()
                >= min;
    }

    private static Predicate<TumorDetails> hasAnyUncategorizedLesions() {
        return tumor -> Optional.ofNullable(tumor.otherLesions()).map(List::isEmpty).orElse(false);
    }

    private static boolean evaluateMetastases(@Nullable final Boolean hasLesions, final Set<String> tumorDoids, final String doidToMatch,
            final DoidModel doidModel) {
        return Optional.ofNullable(hasLesions)
                .map(h -> h && !DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch))
                .orElse(false);
    }
}
