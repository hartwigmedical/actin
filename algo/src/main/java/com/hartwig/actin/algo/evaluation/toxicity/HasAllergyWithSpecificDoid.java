package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Allergy;

import org.jetbrains.annotations.NotNull;

public class HasAllergyWithSpecificDoid implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToFind;

    HasAllergyWithSpecificDoid(@NotNull final DoidModel doidModel, @NotNull final String doidToFind) {
        this.doidModel = doidModel;
        this.doidToFind = doidToFind;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> allergies = Sets.newHashSet();
        for (Allergy allergy : record.clinical().allergies()) {
            for (String doid : allergy.doids()) {
                if (doidModel.doidWithParents(doid).contains(doidToFind)) {
                    allergies.add(allergy.name());
                }
            }
        }

        if (!allergies.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has allergy " + Format.concat(allergies))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no allergies with doid" + doidModel.term(doidToFind))
                .build();
    }
}
