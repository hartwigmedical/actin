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

public class HasSolidPrimaryTumor implements EvaluationFunction {

    static final Set<String> NON_SOLID_CANCER_DOIDS = Sets.newHashSet();
    static final Set<String> WARN_SOLID_CANCER_DOIDS = Sets.newHashSet();

    static {
        NON_SOLID_CANCER_DOIDS.add(DoidConstants.LEUKEMIA_DOID);
        NON_SOLID_CANCER_DOIDS.add(DoidConstants.REFRACTORY_HEMATOLOGIC_CANCER_DOID);
        NON_SOLID_CANCER_DOIDS.add(DoidConstants.BONE_MARROW_CANCER_DOID);

        WARN_SOLID_CANCER_DOIDS.add(DoidConstants.HEMATOLOGIC_CANCER_DOID);
    }

    @NotNull
    private final DoidModel doidModel;

    HasSolidPrimaryTumor(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> tumorDoids = record.clinical().tumor().doids();

        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No tumor location/type configured for patient, unknown if solid primary tumor")
                    .addUndeterminedGeneralMessages("Undetermined solid primary tumor")
                    .build();
        }

        EvaluationResult result = DoidEvaluationFunctions.evaluateForExclusiveMatchWithFailAndWarns(doidModel,
                tumorDoids,
                DoidConstants.CANCER_DOID,
                NON_SOLID_CANCER_DOIDS,
                WARN_SOLID_CANCER_DOIDS);

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has non-solid primary tumor");
            builder.addFailGeneralMessages("Tumor type");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Unclear if tumor type of patient should be considered solid or non-solid");
            builder.addWarnGeneralMessages("Unclear if primary tumor is considered solid");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has solid primary tumor");
            builder.addPassGeneralMessages("Tumor type");
        }

        return builder.build();
    }
}
