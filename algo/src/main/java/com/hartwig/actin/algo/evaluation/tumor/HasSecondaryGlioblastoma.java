package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasSecondaryGlioblastoma implements EvaluationFunction {

    @NotNull
    private final DoidModel doidModel;

    HasSecondaryGlioblastoma(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> tumorDoids = record.clinical().tumor().doids();

        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "No tumor location/type configured for patient, unknown if patient has secondary glioblastoma")
                    .addUndeterminedGeneralMessages("Undetermined secondary glioblastoma")
                    .build();
        }

        for (String tumorDoid : tumorDoids) {
            if (doidModel.doidWithParents(tumorDoid).contains(DoidConstants.GLIOBLASTOMA_DOID)) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addWarnSpecificMessages(
                                "Patient has " + doidModel.resolveTermForDoid(tumorDoid) + ", belonging to " + doidModel.resolveTermForDoid(
                                        DoidConstants.GLIOBLASTOMA_DOID) + ", unclear if this is considered secondary glioblastoma")
                        .addWarnGeneralMessages("Unclear if considered secondary glioblastoma")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no (secondary) glioblastoma")
                .addFailGeneralMessages("Tumor type")
                .build();
    }
}
