package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasHadPriorConditionWithDoid implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToFind;

    HasHadPriorConditionWithDoid(@NotNull final DoidModel doidModel, @NotNull final String doidToFind) {
        this.doidModel = doidModel;
        this.doidToFind = doidToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        String doidTerm = doidModel.resolveTermForDoid(doidToFind);

        Set<String> conditions = Sets.newHashSet();
        for (PriorOtherCondition condition : OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            if (conditionHasDoid(condition, doidToFind)) {
                conditions.add(condition.name());
            }
        }

        if (!conditions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has " + Format.concat(conditions) + ", which belong(s) to category " + doidTerm)
                    .addPassGeneralMessages("Present " + Format.concat(conditions))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no other condition belonging to category " + doidTerm)
                .addFailGeneralMessages("No relevant non-oncological condition")
                .build();
    }

    private boolean conditionHasDoid(@NotNull PriorOtherCondition condition, @NotNull String doidToFind) {
        for (String doid : condition.doids()) {
            if (doidModel.doidWithParents(doid).contains(doidToFind)) {
                return true;
            }
        }
        return false;
    }
}
