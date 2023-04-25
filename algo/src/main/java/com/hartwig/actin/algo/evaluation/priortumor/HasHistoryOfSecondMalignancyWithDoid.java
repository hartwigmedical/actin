package com.hartwig.actin.algo.evaluation.priortumor;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfSecondMalignancyWithDoid implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToMatch;

    HasHistoryOfSecondMalignancyWithDoid(@NotNull final DoidModel doidModel, @NotNull final String doidToMatch) {
        this.doidModel = doidModel;
        this.doidToMatch = doidToMatch;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        String doidTerm = doidModel.resolveTermForDoid(doidToMatch);

        boolean hasMatch = false;
        for (PriorSecondPrimary priorSecondPrimary : record.clinical().priorSecondPrimaries()) {
            if (isDoidMatch(priorSecondPrimary.doids(), doidToMatch)) {
                hasMatch = true;
            }
        }

        EvaluationResult result = hasMatch ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no history of previous malignancy belonging to " + doidTerm);
            builder.addFailGeneralMessages("No specific history of malignancy");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has history of previous malignancy belonging to " + doidTerm);
            builder.addPassGeneralMessages("Present second primary history belonging to " + doidTerm);
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
