package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.Characteristic;
import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.failGeneral;
import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.failSpecific;
import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.passGeneral;
import static com.hartwig.actin.algo.evaluation.othercondition.PriorConditionMessages.passSpecific;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
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

        Set<String> conditions =
                OtherConditionSelector.selectConditionsMatchingDoid(record.clinical().priorOtherConditions(), doidToFind, doidModel);

        if (!conditions.isEmpty()) {
            return EvaluationFactory.pass(passSpecific(Characteristic.CONDITION, conditions, doidTerm), passGeneral(doidTerm));
        }

        return EvaluationFactory.fail(failSpecific(doidTerm), failGeneral());
    }
}
