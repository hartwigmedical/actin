package com.hartwig.actin.algo.evaluation.priortumor;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasHistoryOfSecondMalignancy implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @Nullable
    private final String doidToMatch;
    private final boolean mustBeInactive;

    HasHistoryOfSecondMalignancy(@NotNull final DoidModel doidModel, @Nullable final String doidToMatch, final boolean mustBeInactive) {
        this.doidModel = doidModel;
        this.doidToMatch = doidToMatch;
        this.mustBeInactive = mustBeInactive;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasMatch = false;
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            boolean doidMatch = doidToMatch == null || isDoidMatch(priorSecondPrimary.doids(), doidToMatch);
            boolean activeMatch = !mustBeInactive || !priorSecondPrimary.isActive();
            if (doidMatch && activeMatch) {
                hasMatch = true;
            }
        }

        EvaluationResult result = hasMatch ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages("Patient has no history of second malignancy");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassMessages("Patient has history of second malignancy");
        }

        return builder.build();
    }

    private boolean isDoidMatch(@NotNull Set<String> doids, @NotNull String doidToMatch) {
        for (String doid : doids) {
            if (doidModel.doidWithParents(doid).contains(doidToMatch)) {
                return true;
            }
        }

        return false;
    }
}
