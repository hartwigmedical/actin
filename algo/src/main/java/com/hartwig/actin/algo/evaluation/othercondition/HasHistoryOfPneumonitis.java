package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasHistoryOfPneumonitis implements EvaluationFunction {

    static final String PNEUMONITIS_DOID = "552";

    static final Set<String> TOXICITIES_CAUSING_PNEUMONITIS = Sets.newHashSet();

    static {
        TOXICITIES_CAUSING_PNEUMONITIS.add("pneumonia");
        TOXICITIES_CAUSING_PNEUMONITIS.add("pneumonitis");
    }

    @NotNull
    private final DoidModel doidModel;

    HasHistoryOfPneumonitis(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorOtherCondition condition : OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            for (String doid : condition.doids()) {
                if (doidModel.doidWithParents(doid).contains(PNEUMONITIS_DOID)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has pneumonitis: " + doidModel.resolveTermForDoid(doid))
                            .addPassGeneralMessages("Pneumonitis")
                            .build();
                }
            }
        }

        for (Toxicity toxicity : record.clinical().toxicities()) {
            if (toxicity.source() == ToxicitySource.QUESTIONNAIRE || (toxicity.grade() != null && toxicity.grade() >= 2)) {
                for (String termToFind : TOXICITIES_CAUSING_PNEUMONITIS) {
                    if (toxicity.name().toLowerCase().contains(termToFind.toLowerCase())) {
                        return EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.PASS)
                                .addPassSpecificMessages("Patient has pneumonitis: " + toxicity.name())
                                .addPassGeneralMessages("Pneumonitis")
                                .build();
                    }
                }
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no pneumonitis")
                .addFailGeneralMessages("No pneumonitis")
                .build();
    }
}
