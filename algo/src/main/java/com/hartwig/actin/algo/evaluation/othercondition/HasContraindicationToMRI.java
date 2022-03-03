package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public class HasContraindicationToMRI implements EvaluationFunction {

    static final String KIDNEY_DISEASE_DOID = "557";

    static final Set<String> OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI = Sets.newHashSet("implant", "claustrophobia");

    static final Set<String> ALLERGIES_BEING_CONTRAINDICATIONS_TO_MRI = Sets.newHashSet("contrast agent");

    @NotNull
    private final DoidModel doidModel;

    HasContraindicationToMRI(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorOtherCondition priorOtherCondition : record.clinical().priorOtherConditions()) {
            for (String doid : priorOtherCondition.doids()) {
                if (doidModel.doidWithParents(doid).contains(KIDNEY_DISEASE_DOID)) {
                    return ImmutableEvaluation.builder()
                            .result(EvaluationResult.PASS)
                            .addPassMessages("Patient has a contraindication to MRI due to " + doidModel.term(doid))
                            .build();
                }
            }

            for (String term : OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI) {
                if (priorOtherCondition.name().toLowerCase().contains(term)) {
                    return ImmutableEvaluation.builder()
                            .result(EvaluationResult.PASS)
                            .addPassMessages("Patient has a contraindication to MRI due to condition " + priorOtherCondition.name())
                            .build();
                }
            }
        }

        for (Allergy allergy : record.clinical().allergies()) {
            for (String term : ALLERGIES_BEING_CONTRAINDICATIONS_TO_MRI) {
                if (allergy.name().toLowerCase().contains(term)) {
                    return ImmutableEvaluation.builder()
                            .result(EvaluationResult.PASS)
                            .addPassMessages("Patient has a contraindication to MRI due to allergy " + allergy.name())
                            .build();
                }
            }
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("No potential contraindications to MRI identified")
                .build();
    }
}
