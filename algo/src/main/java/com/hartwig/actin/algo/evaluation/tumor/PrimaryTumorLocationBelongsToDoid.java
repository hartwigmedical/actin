package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class PrimaryTumorLocationBelongsToDoid implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final String doidToMatch;

    PrimaryTumorLocationBelongsToDoid(@NotNull final DoidModel doidModel, @NotNull final String doidToMatch) {
        this.doidModel = doidModel;
        this.doidToMatch = doidToMatch;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> doids = record.clinical().tumor().doids();

        if (doids == null || doids.isEmpty()) {
            return Evaluation.UNDETERMINED;
        } else {
            Evaluation evaluation = Evaluation.FAIL;

            for (String doid : doids) {
                if (doidModel.doidWithParents(doid).contains(doidToMatch)) {
                    evaluation = Evaluation.PASS;
                }
            }
            return evaluation;
        }
    }
}
