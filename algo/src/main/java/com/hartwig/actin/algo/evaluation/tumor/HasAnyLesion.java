package com.hartwig.actin.algo.evaluation.tumor;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasAnyLesion implements EvaluationFunction {

    HasAnyLesion() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasLiverMetastases = record.clinical().tumor().hasLiverLesions();
        Boolean hasCnsMetastases = record.clinical().tumor().hasCnsLesions();
        Boolean hasBrainMetastases = record.clinical().tumor().hasBrainLesions();
        Boolean hasBoneLesions = record.clinical().tumor().hasBoneLesions();
        Boolean hasLungLesions = record.clinical().tumor().hasLungLesions();
        Boolean hasLymphNodeLesions = record.clinical().tumor().hasLymphNodeLesions();
        List<String> otherLesions = record.clinical().tumor().otherLesions();

        if (hasLiverMetastases == null && hasCnsMetastases == null && hasBrainMetastases == null && hasBoneLesions == null
                && hasLungLesions == null && hasLymphNodeLesions == null && otherLesions == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data about lesions is missing")
                    .addUndeterminedGeneralMessages("Missing lesions details")
                    .build();
        }

        boolean hasOtherLesions = otherLesions != null && !otherLesions.isEmpty();
        boolean hasLesions =
                anyTrue(hasLiverMetastases, hasCnsMetastases, hasBrainMetastases, hasBoneLesions, hasLungLesions,
                        hasLymphNodeLesions, hasOtherLesions);
        EvaluationResult result = hasLesions ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient does not have any lesions");
            builder.addFailGeneralMessages("No lesions present");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has at least one lesion");
            builder.addPassGeneralMessages("Lesions present");
        }

        return builder.build();
    }

    private static boolean anyTrue(@Nullable Boolean... booleans) {
        for (Boolean bool : booleans) {
            if (bool != null && bool) {
                return true;
            }
        }
        return false;
    }
}
