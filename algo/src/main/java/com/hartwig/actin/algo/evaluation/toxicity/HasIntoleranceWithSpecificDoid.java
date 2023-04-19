package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasIntoleranceWithSpecificDoid implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToFind;

    HasIntoleranceWithSpecificDoid(@NotNull final DoidModel doidModel, @NotNull final String doidToFind) {
        this.doidModel = doidModel;
        this.doidToFind = doidToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> allergies = Sets.newHashSet();
        for (Intolerance intolerance : record.clinical().intolerances()) {
            for (String doid : intolerance.doids()) {
                if (doidModel.doidWithParents(doid).contains(doidToFind)) {
                    allergies.add(intolerance.name());
                }
            }
        }

        if (!allergies.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Patient has allergy " + Format.concat(allergies) + " belonging to " + doidModel.resolveTermForDoid(doidToFind))
                    .addPassGeneralMessages(
                            "Present allergy " + Format.concat(allergies) + " belonging to " + doidModel.resolveTermForDoid(doidToFind))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no allergies with doid" + doidModel.resolveTermForDoid(doidToFind))
                .addFailGeneralMessages("No allergies belonging to " + doidModel.resolveTermForDoid(doidToFind))
                .build();
    }
}
