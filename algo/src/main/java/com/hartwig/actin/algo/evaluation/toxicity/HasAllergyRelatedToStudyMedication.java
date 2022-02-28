package com.hartwig.actin.algo.evaluation.toxicity;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.Allergy;

import org.jetbrains.annotations.NotNull;

public class HasAllergyRelatedToStudyMedication implements EvaluationFunction {

    @VisibleForTesting
    static final String MEDICATION_CATEGORY = "Medication";
    @VisibleForTesting
    static final String CLINICAL_STATUS_ACTIVE = "Active";

    HasAllergyRelatedToStudyMedication() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (Allergy allergy : record.clinical().allergies()) {
            if (allergy.category().equalsIgnoreCase(MEDICATION_CATEGORY) && allergy.clinicalStatus()
                    .equalsIgnoreCase(CLINICAL_STATUS_ACTIVE)) {
                return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
            }
        }

        return EvaluationFactory.create(EvaluationResult.FAIL);
    }
}
