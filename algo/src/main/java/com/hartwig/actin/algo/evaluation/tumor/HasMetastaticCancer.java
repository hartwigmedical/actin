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
                    .addUndeterminedSpecificMessages("Tumor stage details are missing")
                    .addUndeterminedGeneralMessages("Missing tumor stage details")
                    .build();
        }

        Set<String> tumorDoids = record.clinical().tumor().doids();
        boolean HasStageIIPotentiallyMetastaticCancer =
                DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, STAGE_II_POTENTIALLY_METASTATIC_CANCERS);

        EvaluationResult result;
        if (stage == TumorStage.III || stage.category() == TumorStage.III || stage == TumorStage.IV || stage.category() == TumorStage.IV) {
            result = EvaluationResult.PASS;
        } else if ((stage == TumorStage.II || stage.category() == TumorStage.II) && HasStageIIPotentiallyMetastaticCancer) {
            result = EvaluationResult.WARN;
        } else if ((stage == TumorStage.II || stage.category() == TumorStage.II) && tumorDoids.isEmpty()) { //TODO: Check if this works
            result = EvaluationResult.UNDETERMINED;
        } else {
            result = EvaluationResult.FAIL;
        }

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor stage " + stage + " is considered locally advanced");
            builder.addPassGeneralMessages("Locally advanced cancer");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Could not be determined if tumor stage " + stage + " is considered locally advanced");
            builder.addWarnGeneralMessages("Locally advanced cancer");
        } else if (result == EvaluationResult.UNDETERMINED) {
                builder.addUndeterminedSpecificMessages("Could not be determined if tumor stage " + stage + " is considered locally advanced");
                builder.addUndeterminedGeneralMessages("Locally advanced cancer");
        } else if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor stage " + stage + " is not considered locally advanced");
            builder.addFailGeneralMessages("No locally advanced cancer");
        }

        return builder.build();
    }
}
