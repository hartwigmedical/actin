package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasMeasurableDiseaseRecist implements EvaluationFunction {

    static final Set<String> NON_RECIST_TUMOR_DOIDS = Sets.newHashSet("2531", "1319", "0060058", "9538");

    @NotNull
    private final DoidModel doidModel;

    HasMeasurableDiseaseRecist(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasMeasurableDisease = record.clinical().tumor().hasMeasurableDisease();
        if (hasMeasurableDisease == null) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Data regarding measurable disease is missing")
                    .build();
        }

        EvaluationResult result;
        if (hasMeasurableDisease) {
            result = hasNonRecistDoid(record.clinical().tumor().doids()) ? EvaluationResult.PASS_BUT_WARN : EvaluationResult.PASS;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient does not have measurable disease (RECIST)");
        } else if (result == EvaluationResult.PASS_BUT_WARN) {
            builder.addPassMessages("Patient has measurable disease but has a tumor type that typically does not get measured by RECIST");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient has measurable disease (RECIST)");
        }

        return builder.build();
    }

    private boolean hasNonRecistDoid(@Nullable Set<String> doids) {
        if (doids == null) {
            return false;
        }

        for (String doid : doids) {
            for (String doidToMatch : NON_RECIST_TUMOR_DOIDS) {
                if (doidModel.doidWithParents(doid).contains(doidToMatch)) {
                    return true;
                }
            }
        }

        return false;
    }
}
