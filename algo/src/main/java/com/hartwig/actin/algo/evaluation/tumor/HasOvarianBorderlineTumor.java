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

public class HasOvarianBorderlineTumor implements EvaluationFunction {

    static final Set<String> OVARIAN_BORDERLINE_TYPES = Sets.newHashSet();

    static {
        OVARIAN_BORDERLINE_TYPES.add("Borderline tumor");
        OVARIAN_BORDERLINE_TYPES.add("Borderline ovarian tumor");
    }

    @NotNull
    private final DoidModel doidModel;

    HasOvarianBorderlineTumor(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> tumorDoids = record.clinical().tumor().doids();
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) || (record.clinical().tumor().primaryTumorType() == null
                && record.clinical().tumor().primaryTumorSubType() == null)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has ovarian borderline tumor")
                    .addUndeterminedGeneralMessages("Undetermined ovarian borderline tumor")
                    .build();
        }

        boolean isOvarianCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.OVARIAN_CANCER_DOID);
        boolean hasBorderlineType = TumorTypeEvaluationFunctions.hasTumorWithType(record.clinical().tumor(), OVARIAN_BORDERLINE_TYPES);

        if (isOvarianCancer && hasBorderlineType) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has ovarian borderline tumor")
                    .addPassGeneralMessages("Tumor type")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have ovarian borderline tumor")
                .addFailGeneralMessages("Tumor type")
                .build();
    }
}
