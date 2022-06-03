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

public class HasBoneMetastasesOnly implements EvaluationFunction {

    //TODO: Add otherLesions logic (README)
    HasBoneMetastasesOnly() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasBoneMetastases = record.clinical().tumor().hasBoneLesions();

        if (hasBoneMetastases == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding presence of bone metastases is missing")
                    .addUndeterminedGeneralMessages("Missing bone metastasis data")
                    .build();
        }

        Boolean hasLiverMetastases = record.clinical().tumor().hasLiverLesions();
        Boolean hasCnsMetastases = record.clinical().tumor().hasCnsLesions();
        Boolean hasBrainMetastases = record.clinical().tumor().hasBrainLesions();
        Boolean hasLungLesions = record.clinical().tumor().hasLungLesions();
        List<String> otherLesions = record.clinical().tumor().otherLesions();

        if (hasBoneMetastases && hasLiverMetastases == null && hasCnsMetastases == null && hasBrainMetastases == null
                && hasLungLesions == null && otherLesions == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient has bone lesions but data regarding other lesion locations is missing")
                    .addWarnGeneralMessages("Missing other metastasis data")
                    .build();
        }

        boolean hasOtherLesion = otherLesions != null && !otherLesions.isEmpty();
        boolean hasAnyOtherLesion = anyTrue(hasLiverMetastases, hasCnsMetastases, hasBrainMetastases, hasLungLesions, hasOtherLesion);
        EvaluationResult result = hasBoneMetastases && !hasAnyOtherLesion ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient does not have bone metastases exclusively");
            builder.addFailGeneralMessages("No bone-only metastases");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient only has bone metastases");
            builder.addPassGeneralMessages("Bone-only metastases");
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
