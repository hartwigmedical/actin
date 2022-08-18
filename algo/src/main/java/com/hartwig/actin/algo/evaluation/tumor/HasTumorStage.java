package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.TumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasTumorStage implements EvaluationFunction {

    static final String LIVER_CANCER_DOID = "3571";
    static final String CNS_CANCER_DOID = "3620";
    static final String BRAIN_CANCER_DOID = "1319";
    static final String LUNG_CANCER_DOID = "1324";
    static final String BONE_CANCER_DOID = "184";

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final TumorStage stageToMatch;

    HasTumorStage(@NotNull final DoidModel doidModel, @NotNull final TumorStage stageToMatch) {
        this.doidModel = doidModel;
        this.stageToMatch = stageToMatch;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        TumorStage stage = record.clinical().tumor().stage();

        if (stage == null) {
            stage = resolveTumorStageFromLesions(record.clinical().tumor());
        }

        if (stage == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Tumor stage details are missing")
                    .addUndeterminedGeneralMessages("Missing tumor stage details")
                    .build();
        }

        boolean hasTumorStage = stage == stageToMatch || stage.category() == stageToMatch;

        EvaluationResult result = hasTumorStage ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient tumor stage is not exact stage " + stageToMatch.display());
            builder.addFailGeneralMessages("Inadequate tumor stage");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient tumor stage is exact stage " + stageToMatch.display());
            builder.addPassGeneralMessages("Adequate tumor stage");
        }

        return builder.build();
    }

    @Nullable
    private TumorStage resolveTumorStageFromLesions(@NotNull TumorDetails tumor) {
        Set<String> tumorDoids = tumor.doids();
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return null;
        }

        boolean hasLiverMetastases = evaluateMetastases(tumor.hasLiverLesions(), tumorDoids, LIVER_CANCER_DOID);
        boolean hasCnsMetastases = evaluateMetastases(tumor.hasCnsLesions(), tumorDoids, CNS_CANCER_DOID);
        boolean hasBrainMetastases = evaluateMetastases(tumor.hasBrainLesions(), tumorDoids, BRAIN_CANCER_DOID);
        boolean hasLungMetastases = evaluateMetastases(tumor.hasLungLesions(), tumorDoids, LUNG_CANCER_DOID);
        boolean hasBoneMetastases = evaluateMetastases(tumor.hasBoneLesions(), tumorDoids, BONE_CANCER_DOID);

        if (hasLiverMetastases || hasCnsMetastases || hasBrainMetastases || hasLungMetastases || hasBoneMetastases) {
            return TumorStage.IV;
        }

        return null;
    }

    private boolean evaluateMetastases(@Nullable Boolean hasLesions, @NotNull Set<String> tumorDoids, @NotNull String doidToMatch) {
        if (hasLesions == null) {
            return false;
        }

        return hasLesions && !DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch);
    }
}
