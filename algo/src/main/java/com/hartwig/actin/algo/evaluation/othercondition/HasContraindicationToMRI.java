package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasContraindicationToMRI implements EvaluationFunction {

    static final Set<String> OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI = Sets.newHashSet("implant", "claustrophobia");

    static final Set<String> INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI = Sets.newHashSet("contrast agent");

    @NotNull
    private final DoidModel doidModel;

    HasContraindicationToMRI(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorOtherCondition condition : OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            for (String doid : condition.doids()) {
                if (doidModel.doidWithParents(doid).contains(DoidConstants.KIDNEY_DISEASE_DOID)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to MRI due to " + doidModel.resolveTermForDoid(doid))
                            .addPassGeneralMessages("MRI contraindication")
                            .build();
                }
            }

            for (String term : OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI) {
                if (condition.name().toLowerCase().contains(term)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to MRI due to condition " + condition.name())
                            .addPassGeneralMessages("MRI contraindication")
                            .build();
                }
            }
        }

        for (Intolerance intolerance : record.clinical().intolerances()) {
            for (String term : INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI) {
                if (intolerance.name().toLowerCase().contains(term)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to MRI due to intolerance " + intolerance.name())
                            .addPassGeneralMessages("MRI contraindication")
                            .build();
                }
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No potential contraindications to MRI identified")
                .addFailGeneralMessages("No potential MRI contraindication")
                .build();
    }
}
