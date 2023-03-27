package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.hasConfiguredDoids;
import static com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfDoidType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.Nullable;

class TumorStageDerivationFunction {

    private final Map<Predicate<TumorDetails>, Set<TumorStage>> derivationRules;

    private TumorStageDerivationFunction(Map<Predicate<TumorDetails>, Set<TumorStage>> derivationRules) {
        this.derivationRules = derivationRules;
    }

    static TumorStageDerivationFunction create(DoidModel doidModel) {
        return new TumorStageDerivationFunction(Map.of(hasExactlyCategorizedLesions(0, doidModel).and(hasNoUncategorizedLesions()),
                Set.of(TumorStage.I, TumorStage.II),
                hasExactlyCategorizedLesions(1, doidModel),
                Set.of(TumorStage.III, TumorStage.IV),
                hasAtLeastCategorizedLesions(2, doidModel),
                Set.of(TumorStage.IV)));
    }

    Stream<TumorStage> apply(TumorDetails tumor) {
        return hasConfiguredDoids(tumor.doids()) && hasNoTumorStage(tumor) ? derivationRules.entrySet()
                .stream()
                .filter(rule -> rule.getKey().test(tumor))
                .map(Map.Entry::getValue)
                .flatMap(Set::stream) : Stream.empty();
    }

    private static boolean hasNoTumorStage(TumorDetails tumor) {
        return tumor.stage() == null;
    }

    private static Predicate<TumorDetails> hasAtLeastCategorizedLesions(int min, DoidModel doidModel) {
        return tumor -> lesionCount(doidModel, tumor) >= min;
    }

    private static Predicate<TumorDetails> hasExactlyCategorizedLesions(int count, DoidModel doidModel) {
        return tumor -> lesionCount(doidModel, tumor) == count;
    }

    private static long lesionCount(DoidModel doidModel, TumorDetails tumor) {
        return Stream.of(Maps.immutableEntry(tumor.hasLiverLesions(), DoidConstants.LIVER_CANCER_DOID),
                        Maps.immutableEntry(tumor.hasLymphNodeLesions(), DoidConstants.LYMPH_NODE_CANCER_DOID),
                        Maps.immutableEntry(tumor.hasCnsLesions(), DoidConstants.CNS_CANCER_DOID),
                        Maps.immutableEntry(tumor.hasBrainLesions(), DoidConstants.BRAIN_CANCER_DOID),
                        Maps.immutableEntry(tumor.hasLungLesions(), DoidConstants.LUNG_CANCER_DOID),
                        Maps.immutableEntry(tumor.hasBoneLesions(), DoidConstants.BONE_CANCER_DOID))
                .filter(entry -> evaluateMetastases(entry.getKey(), tumor.doids(), entry.getValue(), doidModel))
                .count();
    }

    private static Predicate<TumorDetails> hasNoUncategorizedLesions() {
        return tumor -> Optional.ofNullable(tumor.otherLesions()).map(List::isEmpty).orElse(true);
    }

    private static boolean evaluateMetastases(@Nullable Boolean hasLesions, Set<String> tumorDoids, String doidToMatch,
            DoidModel doidModel) {
        return Optional.ofNullable(hasLesions).map(h -> h && !isOfDoidType(doidModel, tumorDoids, doidToMatch)).orElse(false);
    }
}
