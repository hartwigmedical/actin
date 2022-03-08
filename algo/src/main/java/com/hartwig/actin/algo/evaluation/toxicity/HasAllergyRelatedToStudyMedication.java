package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
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
        Set<String> allergies = Sets.newHashSet();
        for (Allergy allergy : record.clinical().allergies()) {
            if (allergy.category().equalsIgnoreCase(MEDICATION_CATEGORY) && allergy.clinicalStatus()
                    .equalsIgnoreCase(CLINICAL_STATUS_ACTIVE)) {
                allergies.add(allergy.name());
            }
        }

        if (!allergies.isEmpty()) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Patient has medication-related allergies: " + concat(allergies) + ". "
                            + "Currently not determined if this could be related to potential study medication")
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("Patient has no known allergies with category 'medication'")
                .build();
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
