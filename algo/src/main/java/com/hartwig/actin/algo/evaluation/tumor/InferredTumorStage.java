package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.Nullable;

class InferredTumorStage {

    private final List<Function<TumorDetails, Set<TumorStage>>> inferenceRules;

    private InferredTumorStage(final List<Function<TumorDetails, Set<TumorStage>>> inferenceRules) {
        this.inferenceRules = inferenceRules;
    }

    Set<TumorStage> from(final TumorDetails tumor) {
        return Optional.ofNullable(tumor.stage())
                .orElse(DoidEvaluationFunctions.hasConfiguredDoids(tumor.doids()) ? inferenceRules.stream()
                        .map(rule -> rule.apply(tumor))
                        .map(Set::stream)
                        .findFirst()
                        .flatMap(Set::stream)
                        .stream()
                        .collect(Collectors.toSet());
    }

    static InferredTumorStage defaultRules(final DoidModel doidModel) {
        return new InferredTumorStage(List.of(InferredTumorStage::hasNoLesions));
    }

    private static Set<TumorStage> hasNoLesions(final TumorDetails tumorDetails) {
        if (!hasLesions(tumorDetails)) {
            return Set.of(TumorStage.I, TumorStage.II);
        } else {
            return Set.of();
        }
    }

    private static Optional<TumorStage> lesionsPresentOfCategoryLymphNodeOrSingleLocation(final TumorDetails tumorDetails,
            final DoidModel doidModel) {
        if (hasLesions(tumorDetails)) {
            if (DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDetails.doids(), DoidConstants.LYMPH_NODE_CANCER_DOID)) {
                return Optional.of(TumorStage.II);
            } else if ()
        } else {
            return Optional.empty();
        }
    }

    private static boolean hasLesions(final TumorDetails tumorDetails) {
        return nullSafeBoolean(tumorDetails.hasBrainLesions()) || nullSafeBoolean(tumorDetails.hasLungLesions()) || nullSafeBoolean(
                tumorDetails.hasCnsLesions()) || nullSafeBoolean(tumorDetails.hasBoneLesions())
                || nullSafeBoolean(tumorDetails.hasLiverLesions())
                || Optional.ofNullable(tumorDetails.otherLesions()).orElse(Collections.emptyList()).size() > 1;
    }

    private static Boolean nullSafeBoolean(@Nullable final Boolean maybeBoolean) {
        return Optional.ofNullable(maybeBoolean).orElse(false);
    }

}
