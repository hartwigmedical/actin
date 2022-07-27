package com.hartwig.actin.algo.evaluation.infection;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public class HasKnownEBVInfection implements EvaluationFunction {

    static final Set<String> EBV_TERMS = Sets.newHashSet();

    static {
        EBV_TERMS.add("EBV");
        EBV_TERMS.add("Epstein Barr");
    }

    HasKnownEBVInfection() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorOtherCondition condition : OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            for (String ebvTerm : EBV_TERMS) {
                if (condition.name().toLowerCase().contains(ebvTerm.toLowerCase())) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has known EBV infection: " + condition.name())
                            .addPassGeneralMessages("Present EBV infection")
                            .build();
                }
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no known EBV infection")
                .addFailGeneralMessages("Requested infection(s) not present")
                .build();
    }
}
