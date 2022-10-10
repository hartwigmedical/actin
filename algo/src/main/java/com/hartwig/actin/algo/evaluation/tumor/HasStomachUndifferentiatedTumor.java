package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasStomachUndifferentiatedTumor implements EvaluationFunction {

    static final Set<String> UNDIFFERENTIATED_TYPES = Sets.newHashSet();

    static {
        UNDIFFERENTIATED_TYPES.add("Undifferentiated");
    }

    @NotNull
    private final DoidModel doidModel;

    HasStomachUndifferentiatedTumor(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> tumorDoids = record.clinical().tumor().doids();
        if ((!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) || (record.clinical().tumor().primaryTumorType() == null
                && record.clinical().tumor().primaryTumorSubType() == null)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has undifferentiated stomach tumor")
                    .addUndeterminedGeneralMessages("Undifferentiated stomach tumor")
                    .build();
        }

        boolean isStomachCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.STOMACH_CANCER_DOID);
        boolean isUndifferentiatedType = TumorTypeEvaluationFunctions.hasTumorWithType(record.clinical().tumor(), UNDIFFERENTIATED_TYPES);

        if (isStomachCancer && isUndifferentiatedType) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has undifferentiated stomach tumor")
                    .addPassGeneralMessages("Undifferentiated stomach tumor")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have undifferentiated stomach tumor")
                .addFailGeneralMessages("Undifferentiated stomach tumor")
                .build();
    }
}
