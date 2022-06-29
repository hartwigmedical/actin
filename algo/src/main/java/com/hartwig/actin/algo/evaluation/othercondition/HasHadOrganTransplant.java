package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
        List<PriorOtherCondition> clinicallyRelevant =
                OtherConditionFunctions.selectClinicallyRelevant(record.clinical().priorOtherConditions());
        for (PriorOtherCondition priorOtherCondition : clinicallyRelevant) {
            if (priorOtherCondition.category().equals(ORGAN_TRANSPLANT_CATEGORY)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has had an organ transplant")
                        .addPassGeneralMessages("Organ transplant")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not had an organ transplant")
                .addFailGeneralMessages("No organ transplant")
                .build();
    }
}
