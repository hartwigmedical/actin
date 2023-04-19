package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadCompleteResection implements EvaluationFunction {

    static final String COMPLETE_RESECTION = "complete resection";

    static final String RESECTION_KEYWORD = "resection";

    HasHadCompleteResection() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadCompleteResection = false;
        boolean hasHadPotentialCompleteResection = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.name().equalsIgnoreCase(COMPLETE_RESECTION)) {
                hasHadCompleteResection = true;
            }

            if (treatment.name().toLowerCase().contains(RESECTION_KEYWORD.toLowerCase())) {
                hasHadPotentialCompleteResection = true;
            }

            if (treatment.categories().contains(TreatmentCategory.SURGERY) && treatment.name().isEmpty()) {
                hasHadPotentialCompleteResection = true;
            }
        }

        if (hasHadCompleteResection) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has had a complete resection")
                    .addPassGeneralMessages("Had had complete resection")
                    .build();
        } else if (hasHadPotentialCompleteResection) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not be determined whether patient has had a complete resection")
                    .addUndeterminedSpecificMessages("Complete resection undetermined")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not had a complete resection")
                .addFailGeneralMessages("Has not had complete resection")
                .build();
    }
}
