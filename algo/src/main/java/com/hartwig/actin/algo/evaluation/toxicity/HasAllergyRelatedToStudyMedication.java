package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Intolerance;

import org.jetbrains.annotations.NotNull;

public class HasAllergyRelatedToStudyMedication implements EvaluationFunction {

    static final String DRUG_ALLERGY_DOID = "0060500";

    static final String MEDICATION_CATEGORY = "Medication";
    static final String CLINICAL_STATUS_ACTIVE = "Active";

    HasAllergyRelatedToStudyMedication() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> allergies = Sets.newHashSet();
        for (Intolerance intolerance : record.clinical().intolerances()) {
            if (intolerance.clinicalStatus().equalsIgnoreCase(CLINICAL_STATUS_ACTIVE)) {
                boolean doidMatch = intolerance.doids().contains(DRUG_ALLERGY_DOID);
                boolean categoryMatch = intolerance.category().equalsIgnoreCase(MEDICATION_CATEGORY);

                if (doidMatch || categoryMatch) {
                    allergies.add(intolerance.name());
                }
            }
        }

        if (!allergies.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has medication-related allergies: " + Format.concat(allergies) + ". "
                            + "Currently not determined if this could be related to potential study medication")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no known allergies with category 'medication'")
                .build();
    }
}
