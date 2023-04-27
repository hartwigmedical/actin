package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Collection;
import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

import org.jetbrains.annotations.NotNull;

public class HasMinimumSitesWithLesions implements EvaluationFunction {

    private static final String EVALUATION_GENERAL_MESSAGE = "Minimum sites with lesions";

    private final int minimumSitesWithLesions;

    HasMinimumSitesWithLesions(int minimumSitesWithLesions) {
        this.minimumSitesWithLesions = minimumSitesWithLesions;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        TumorDetails tumorDetails = record.clinical().tumor();
        int distinctCategorizedLesionLocations = (int) Stream.of(tumorDetails.hasBoneLesions(),
                tumorDetails.hasBrainLesions(),
                tumorDetails.hasCnsLesions(),
                tumorDetails.hasLiverLesions(),
                tumorDetails.hasLungLesions(),
                tumorDetails.hasLymphNodeLesions()).filter(Boolean.TRUE::equals).count();

        int otherLesionCount = (int) Stream.concat(Stream.ofNullable(tumorDetails.otherLesions()).flatMap(Collection::stream),
                        Stream.ofNullable(tumorDetails.biopsyLocation()))
                .filter(lesion -> !(lesion.toLowerCase().contains("lymph") && Boolean.TRUE.equals(tumorDetails.hasLymphNodeLesions())))
                .count();

        int sitesWithLesionsLowerBound = distinctCategorizedLesionLocations + Math.min(otherLesionCount, 1);
        int sitesWithLesionsUpperBound = distinctCategorizedLesionLocations + otherLesionCount + 1;

        if (sitesWithLesionsLowerBound >= minimumSitesWithLesions) {
            return EvaluationFactory.pass(String.format("Patient has at least %d lesion sites which meets threshold of %d",
                    sitesWithLesionsLowerBound,
                    minimumSitesWithLesions), EVALUATION_GENERAL_MESSAGE);
        } else if (sitesWithLesionsUpperBound >= minimumSitesWithLesions) {
            return EvaluationFactory.undetermined(String.format(
                    "Patient has between %d and %d lesion sites, so it is unclear if threshold of %d is met",
                    sitesWithLesionsLowerBound,
                    sitesWithLesionsUpperBound,
                    minimumSitesWithLesions), EVALUATION_GENERAL_MESSAGE);
        } else {
            return EvaluationFactory.fail(String.format("Patient has at most %d lesion sites, which is less than threshold %d",
                    sitesWithLesionsUpperBound,
                    minimumSitesWithLesions), EVALUATION_GENERAL_MESSAGE);
        }
    }
}
