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
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasContraindicationToCT implements EvaluationFunction {

    static final Set<String> OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT = Sets.newHashSet("claustrophobia");

    static final Set<String> INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT = Sets.newHashSet("contrast agent");

    static final Set<String> MEDICATIONS_BEING_CONTRAINDICATIONS_TO_CT = Sets.newHashSet("metformin");

    static final Set<String> COMPLICATIONS_BEING_CONTRAINDICATIONS_TO_CT = Sets.newHashSet("hyperthyroidism");

    @NotNull
    private final DoidModel doidModel;

    HasContraindicationToCT(@NotNull final DoidModel doidModel) {
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
                            .addPassSpecificMessages("Patient has a contraindication to CT due to " + doidModel.resolveTermForDoid(doid))
                            .addPassGeneralMessages("Potential CT contraindication: " + doidModel.resolveTermForDoid(doid))
                            .build();
                }
            }

            for (String term : OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT) {
                if (condition.name().toLowerCase().contains(term)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to CT due to condition " + condition.name())
                            .addPassGeneralMessages("Potential CT contraindication: " + condition.name())
                            .build();
                }
            }
        }

        for (Intolerance intolerance : record.clinical().intolerances()) {
            for (String term : INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT) {
                if (intolerance.name().toLowerCase().contains(term)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to CT due to intolerance " + intolerance.name())
                            .addPassGeneralMessages("Potential CT contraindication: " + intolerance.name())
                            .build();
                }
            }
        }

        for (Medication medication : record.clinical().medications()) {
            for (String term : MEDICATIONS_BEING_CONTRAINDICATIONS_TO_CT) {
                if (medication.name().toLowerCase().contains(term)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to CT due to medication " + medication.name())
                            .addPassGeneralMessages("Potential CT contraindication: " + medication.name())
                            .build();
                }
            }
        }

        if (record.clinical().complications() != null) {
            for (Complication complication : record.clinical().complications()) {
                for (String term : COMPLICATIONS_BEING_CONTRAINDICATIONS_TO_CT) {
                    if (complication.name().toLowerCase().contains(term)) {
                        return EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.PASS)
                                .addPassSpecificMessages("Patient has a contraindication to CT due to complication " + complication.name())
                                .addPassGeneralMessages("Potential CT contraindication: " + complication.name())
                                .build();
                    }
                }
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No potential contraindications to CT identified")
                .addFailGeneralMessages("No potential contraindications to CT")
                .build();
    }
}
