package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasMeasurableDiseaseRecist implements EvaluationFunction {

    static final Set<String> NON_RECIST_TUMOR_DOIDS = Sets.newHashSet("2531", "1319", "0060058", "9538");

    @NotNull
    private final DoidModel doidModel;

    HasMeasurableDiseaseRecist(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Boolean hasMeasurableDisease = record.clinical().tumor().hasMeasurableDisease();

        if (hasMeasurableDisease == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Data regarding measurable disease is missing")
                    .addUndeterminedGeneralMessages("Missing measurable disease details")
                    .build();
        }

        EvaluationResult result;
        if (hasMeasurableDisease) {
            result = hasNonRecistDoid(record.clinical().tumor().doids()) ? EvaluationResult.WARN : EvaluationResult.PASS;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no measurable disease");
            builder.addFailGeneralMessages("No measurable disease");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Patient has measurable disease, "
                    + "but given the patient's tumor type uncertain if this has been evaluated against RECIST?");
            builder.addWarnGeneralMessages("Measurable disease");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has measurable disease");
            builder.addPassGeneralMessages("Measurable disease");
        }

        return builder.build();
    }

    private boolean hasNonRecistDoid(@Nullable Set<String> doids) {
        if (doids == null) {
            return false;
        }

        for (String doid : doids) {
            Set<String> doidTree = doidModel.doidWithParents(doid);
            for (String doidToMatch : NON_RECIST_TUMOR_DOIDS) {
                if (doidTree.contains(doidToMatch)) {
                    return true;
                }
            }
        }

        return false;
    }
}
