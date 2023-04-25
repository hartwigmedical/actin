package com.hartwig.actin.algo.evaluation.othercondition;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public class HasHadOrganTransplant implements EvaluationFunction {

    @VisibleForTesting
    static final String ORGAN_TRANSPLANT_CATEGORY = "Organ transplant";

    @Nullable
    private final Integer minYear;

    HasHadOrganTransplant(@Nullable final Integer minYear) {
        this.minYear = minYear;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasOrganTransplantWithUnknownYear = false;
        for (PriorOtherCondition condition : OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            if (condition.category().equals(ORGAN_TRANSPLANT_CATEGORY)) {

                boolean isPass = minYear == null;
                if (minYear != null) {
                    Integer conditionYear = condition.year();
                    if (conditionYear == null) {
                        hasOrganTransplantWithUnknownYear = true;
                    } else {
                        isPass = conditionYear >= minYear;
                    }
                }

                if (isPass) {
                    ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(EvaluationResult.PASS);

                    if (minYear != null) {
                        builder.addPassSpecificMessages("Patient has had an organ transplant at some point in or after " + minYear);
                        builder.addPassGeneralMessages("Patient had organ transplant in or after " + minYear);
                    } else {
                        builder.addPassSpecificMessages("Patient has had an organ transplant");
                        builder.addPassGeneralMessages("Has had organ transplant");
                    }
                    return builder.build();
                }
            }
        }

        if (hasOrganTransplantWithUnknownYear) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Patient has had organ transplant but in unclear year")
                    .addUndeterminedGeneralMessages("Date of previous organ transplant unknown")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has not had an organ transplant")
                .addFailGeneralMessages("No organ transplant")
                .build();
    }
}
