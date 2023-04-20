package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadPartialResection implements EvaluationFunction {

    static final String PARTIAL_RESECTION = "partial resection";

    static final String RESECTION_KEYWORD = "resection";

    HasHadPartialResection() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadPartialResection = false;
        boolean hasHadPotentialPartialResection = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.name().equalsIgnoreCase(PARTIAL_RESECTION)) {
                hasHadPartialResection = true;
            }

            if (treatment.name().toLowerCase().contains(RESECTION_KEYWORD.toLowerCase())) {
                hasHadPotentialPartialResection = true;
            }

            if (treatment.categories().contains(TreatmentCategory.SURGERY) && treatment.name().isEmpty()) {
                hasHadPotentialPartialResection = true;
            }
        }

        if (hasHadPartialResection) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has had a partial resection")
                    .addPassGeneralMessages("Has had partial resection")
                    .build();
        } else if (hasHadPotentialPartialResection) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not be determined whether patient has had a partial resection")
                    .addUndeterminedGeneralMessages("Partial resection undetermined")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not had a partial resection")
                .addFailGeneralMessages("Has not had partial resection")
                .build();
    }
}
