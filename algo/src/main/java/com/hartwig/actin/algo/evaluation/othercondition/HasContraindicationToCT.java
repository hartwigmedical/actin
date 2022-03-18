package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public class HasContraindicationToCT implements EvaluationFunction {

    static final String KIDNEY_DISEASE_DOID = "557";

    static final Set<String> OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT = Sets.newHashSet("claustrophobia");

    static final Set<String> ALLERGIES_BEING_CONTRAINDICATIONS_TO_CT = Sets.newHashSet("contrast agent");

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
        for (PriorOtherCondition priorOtherCondition : record.clinical().priorOtherConditions()) {
            for (String doid : priorOtherCondition.doids()) {
                if (doidModel.doidWithParents(doid).contains(KIDNEY_DISEASE_DOID)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to CT due to " + doidModel.term(doid))
                            .addPassGeneralMessages("CT contraindication")
                            .build();
                }
            }

            for (String term : OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT) {
                if (priorOtherCondition.name().toLowerCase().contains(term)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to CT due to condition " + priorOtherCondition.name())
                            .addPassGeneralMessages("CT contraindication")
                            .build();
                }
            }
        }

        for (Allergy allergy : record.clinical().allergies()) {
            for (String term : ALLERGIES_BEING_CONTRAINDICATIONS_TO_CT) {
                if (allergy.name().toLowerCase().contains(term)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to CT due to allergy " + allergy.name())
                            .addPassGeneralMessages("CT contraindication")
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
                            .addPassGeneralMessages("CT contraindication")
                            .build();
                }
            }
        }

        for (Complication complication : record.clinical().complications()) {
            for (String term : COMPLICATIONS_BEING_CONTRAINDICATIONS_TO_CT) {
                if (complication.name().toLowerCase().contains(term)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has a contraindication to CT due to complication " + complication.name())
                            .addPassGeneralMessages("CT contraindication")
                            .build();
                }
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No potential contraindications to CT identified")
                .addFailGeneralMessages("No potential CT contraindication")
                .build();
    }
}
