package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasMeasurableDiseaseRecist implements EvaluationFunction {

    static final Set<String> NON_RECIST_TUMOR_DOIDS = Sets.newHashSet();

    static {
        NON_RECIST_TUMOR_DOIDS.add(DoidConstants.HEMATOLOGIC_CANCER_DOID);
        NON_RECIST_TUMOR_DOIDS.add(DoidConstants.BRAIN_CANCER_DOID);
        NON_RECIST_TUMOR_DOIDS.add(DoidConstants.LYMPHOMA_DOID);
        NON_RECIST_TUMOR_DOIDS.add(DoidConstants.MULTIPLE_MYELOMA_DOID);
    }

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
            result = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, record.clinical().tumor().doids(), NON_RECIST_TUMOR_DOIDS)
                    ? EvaluationResult.WARN
                    : EvaluationResult.PASS;
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
}
