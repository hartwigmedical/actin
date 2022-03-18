package com.hartwig.actin.algo.evaluation.othercondition;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public class HasHadOrganTransplant implements EvaluationFunction {

    @VisibleForTesting
    static final String ORGAN_TRANSPLANT_CATEGORY = "Organ transplant";

    HasHadOrganTransplant() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorOtherCondition priorOtherCondition : record.clinical().priorOtherConditions()) {
            if (priorOtherCondition.category().equals(ORGAN_TRANSPLANT_CATEGORY)) {
                return ImmutableEvaluation.builder()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has had an organ transplant")
                        .addPassGeneralMessages("Organ transplant")
                        .build();
            }
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not had an organ transplant")
                .addFailGeneralMessages("No organ transplant")
                .build();
    }
}
