package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadTyrosineKinaseTreatment implements EvaluationFunction {

    HasHadTyrosineKinaseTreatment() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadTargetedTherapy = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(TreatmentCategory.TARGETED_THERAPY)) {
                hasHadTargetedTherapy = true;
            }
        }

        if (hasHadTargetedTherapy) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Patient has had targeted therapy but not sure if this is a tyrosine kinase inhibitor")
                    .build();
        } else {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.FAIL)
                    .addFailMessages("Patient has not received any targeted therapy so certainly no tyrosine kinase inhibitor")
                    .build();
        }
    }
}
