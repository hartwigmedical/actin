package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Allergy;

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
        for (Allergy allergy : record.clinical().allergies()) {
            if (allergy.clinicalStatus().equalsIgnoreCase(CLINICAL_STATUS_ACTIVE)) {
                boolean doidMatch = allergy.doids().contains(DRUG_ALLERGY_DOID);
                boolean categoryMatch = allergy.category().equalsIgnoreCase(MEDICATION_CATEGORY);

                if (doidMatch || categoryMatch) {
                    allergies.add(allergy.name());
                }
            }
        }

        if (!allergies.isEmpty()) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Patient has medication-related allergies: " + Format.concat(allergies) + ". "
                            + "Currently not determined if this could be related to potential study medication")
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("Patient has no known allergies with category 'medication'")
                .build();
    }
}
