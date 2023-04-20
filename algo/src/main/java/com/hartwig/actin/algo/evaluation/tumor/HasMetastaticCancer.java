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
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasMetastaticCancer implements EvaluationFunction {

    static final Set<String> STAGE_II_POTENTIALLY_METASTATIC_CANCERS = Sets.newHashSet();
    private static final String METASTATIC_CANCER = "Metastatic cancer";
    private static final String NOT_METASTATIC_CANCER = "Not metastatic cancer";

    static {
        STAGE_II_POTENTIALLY_METASTATIC_CANCERS.add(DoidConstants.BRAIN_CANCER_DOID);
        STAGE_II_POTENTIALLY_METASTATIC_CANCERS.add(DoidConstants.HEAD_AND_NECK_CANCER_DOID);
    }

    @NotNull
    private final DoidModel doidModel;

    HasMetastaticCancer(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        TumorStage stage = record.clinical().tumor().stage();

        if (stage == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Tumor stage details are missing, if cancer is metastatic cannot be determined")
                    .addUndeterminedGeneralMessages("Undetermined metastatic cancer")
                    .build();
        }

        EvaluationResult result;
        if (isStageMatch(stage, TumorStage.III) || isStageMatch(stage, TumorStage.IV)) {
            result = EvaluationResult.PASS;
        } else if (isStageMatch(stage, TumorStage.II)) {
            Set<String> tumorDoids = record.clinical().tumor().doids();
            if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
                result = EvaluationResult.UNDETERMINED;
            } else if (DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, STAGE_II_POTENTIALLY_METASTATIC_CANCERS)) {
                result = EvaluationResult.WARN;
            } else {
                result = EvaluationResult.FAIL;
            }
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);

        if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor stage " + stage + " is considered metastatic");
            builder.addPassGeneralMessages(METASTATIC_CANCER);
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Could not be determined if tumor stage " + stage + " is considered metastatic");
            builder.addWarnGeneralMessages("Undetermined " + METASTATIC_CANCER);
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("Could not be determined if tumor stage " + stage + " is considered metastatic");
            builder.addUndeterminedGeneralMessages("Undetermined " + METASTATIC_CANCER);
        } else if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor stage " + stage + " is not considered metastatic");
            builder.addFailGeneralMessages(NOT_METASTATIC_CANCER);
        }

        return builder.build();
    }

    private static boolean isStageMatch(@NotNull TumorStage stage, @NotNull TumorStage stageToMatch) {
        return stage == stageToMatch || stage.category() == stageToMatch;
    }
}
