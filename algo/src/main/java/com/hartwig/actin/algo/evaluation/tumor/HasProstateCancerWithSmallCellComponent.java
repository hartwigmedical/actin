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

public class HasProstateCancerWithSmallCellComponent implements EvaluationFunction {

    static final String SMALL_CELL_DETAILS = "small cell";

    static final Set<Set<String>> PROSTATE_WARN_DOID_SETS = Sets.newHashSet();

    static {
        PROSTATE_WARN_DOID_SETS.add(Sets.newHashSet(DoidConstants.PROSTATE_NEUROENDOCRINE_NEOPLASM));
        PROSTATE_WARN_DOID_SETS.add(Sets.newHashSet(DoidConstants.PROSTATE_CANCER_DOID, DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID));
        PROSTATE_WARN_DOID_SETS.add(Sets.newHashSet(DoidConstants.PROSTATE_CANCER_DOID, DoidConstants.NEUROENDOCRINE_TUMOR_DOID));
    }

    @NotNull
    private final DoidModel doidModel;

    HasProstateCancerWithSmallCellComponent(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> tumorDoids = record.clinical().tumor().doids();
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has prostate cancer with small cell histology")
                    .addUndeterminedGeneralMessages("Undetermined prostate cancer with small cell histology")
                    .build();
        }

        boolean isProstateSmallCellCarcinoma =
                DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.PROSTATE_SMALL_CELL_CARCINOMA_DOID);
        boolean hasProstateCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.PROSTATE_CANCER_DOID);
        boolean hasSmallCellDetails =
                TumorTypeEvaluationFunctions.hasTumorWithDetails(record.clinical().tumor(), Sets.newHashSet(SMALL_CELL_DETAILS));

        if (isProstateSmallCellCarcinoma || (hasProstateCancer && hasSmallCellDetails)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassGeneralMessages("Patient has prostate cancer with small cell histology")
                    .addPassSpecificMessages("Tumor type")
                    .build();
        }

        boolean hasProstateWarnType = false;
        for (Set<String> warnDoidCombination : PROSTATE_WARN_DOID_SETS) {
            boolean matchesWithCombination = true;
            for (String warnDoid : warnDoidCombination) {
                if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, warnDoid)) {
                    matchesWithCombination = false;
                }
            }
            if (matchesWithCombination) {
                hasProstateWarnType = true;
            }
        }

        if (hasProstateWarnType) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnGeneralMessages("Patient has prostate cancer but potentially no small cell histology")
                    .addWarnSpecificMessages("Undetermined prostate cancer with small cell histology")
                    .build();
        }

        boolean isExactProstateCancer = DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.PROSTATE_CANCER_DOID);
        if (isExactProstateCancer) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedGeneralMessages("Patient has prostate cancer but with no configured histology subtype")
                    .addUndeterminedSpecificMessages("Undetermined prostate cancer with small cell histology")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no prostate cancer with small cell histology")
                .addFailGeneralMessages("Tumor type")
                .build();
    }
}
