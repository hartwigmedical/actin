package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.treatment.input.datamodel.TumorTypeInput;

import org.jetbrains.annotations.NotNull;

public class HasCancerOfUnknownPrimary implements EvaluationFunction {

    static final String CANCER_DOID = "162";
    static final String ORGAN_SYSTEM_CANCER_DOID = "0050686";

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final TumorTypeInput categoryOfCUP;

    HasCancerOfUnknownPrimary(@NotNull final DoidModel doidModel, @NotNull final TumorTypeInput categoryOfCUP) {
        this.doidModel = doidModel;
        this.categoryOfCUP = categoryOfCUP;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> doids = record.clinical().tumor().doids();

        if (doids == null || doids.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No tumor type known for patient")
                    .addUndeterminedGeneralMessages("Unknown tumor type")
                    .build();
        }

        if (doids.equals(Sets.newHashSet(CANCER_DOID))) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Patient has tumor type 'cancer' configured")
                    .addWarnGeneralMessages("Unknown if CUP")
                    .build();
        }

        boolean isMatch = true;
        for (String doid : doids) {
            if (doidModel.doidWithParents(doid).contains(ORGAN_SYSTEM_CANCER_DOID)) {
                isMatch = false;
            }

            if (!doidModel.doidWithParents(doid).contains(categoryOfCUP.doid())) {
                isMatch = false;
            }
        }

        EvaluationResult result = isMatch ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has no cancer of unknown primary of category " + categoryOfCUP.display());
            builder.addFailGeneralMessages("Tumor type is no CUP");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has cancer of unknown primary of category " + categoryOfCUP.display());
            builder.addPassGeneralMessages("Tumor type is CUP");
        }

        return builder.build();
    }
}

