package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasProstateCancerWithSmallCellHistology implements EvaluationFunction {

    static final String PROSTATE_CANCER_DOID = "10283";
    static final String PROSTATE_SMALL_CELL_CARCINOMA = "7141";

    static final Set<Set<String>> PROSTATE_WARN_DOID_SETS = Sets.newHashSet();

    static {
        PROSTATE_WARN_DOID_SETS.add(Sets.newHashSet("2922"));
        PROSTATE_WARN_DOID_SETS.add(Sets.newHashSet("10283", "1800"));
        PROSTATE_WARN_DOID_SETS.add(Sets.newHashSet("10283", "169"));
    }

    @NotNull
    private final DoidModel doidModel;

    HasProstateCancerWithSmallCellHistology(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> patientDoids = record.clinical().tumor().doids();
        if (patientDoids == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has prostate cancer with small cell histology")
                    .addUndeterminedGeneralMessages("Tumor type")
                    .build();
        }

        Set<String> expanded = Sets.newHashSet();
        for (String doid : patientDoids) {
            expanded.addAll(doidModel.doidWithParents(doid));
        }

        boolean hasExactDoidMatch = false;
        if (expanded.contains(PROSTATE_CANCER_DOID)) {
            String extraDetails = record.clinical().tumor().primaryTumorExtraDetails();
            boolean hasSmallCellHistology = (extraDetails != null && extraDetails.toLowerCase().contains("small cell"));
            if (expanded.contains(PROSTATE_SMALL_CELL_CARCINOMA) || hasSmallCellHistology) {
                hasExactDoidMatch = true;
            }
        }

        boolean hasSuspiciousDoidMatch = false;
        for (Set<String> warnDoids : PROSTATE_WARN_DOID_SETS) {
            boolean containsEntireSet = true;
            for (String warnDoid : warnDoids) {
                containsEntireSet = containsEntireSet && expanded.contains(warnDoid);
            }
            if (containsEntireSet) {
                hasSuspiciousDoidMatch = true;
                break;
            }
        }

        boolean hasUndeterminedDoidMatch = false;
        if (patientDoids.contains(PROSTATE_CANCER_DOID)) {
            hasUndeterminedDoidMatch = true;
        }

        if (hasExactDoidMatch) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassGeneralMessages("Patient has prostate cancer with small cell histology")
                    .addPassSpecificMessages("Tumor type")
                    .build();
        } else if (hasSuspiciousDoidMatch) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnGeneralMessages("Patient has prostate cancer but potentially no small cell histology")
                    .addWarnSpecificMessages("Tumor type")
                    .build();
        } else if (hasUndeterminedDoidMatch) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedGeneralMessages("Patient has prostate cancer but with no configured histology subtype")
                    .addUndeterminedSpecificMessages("Tumor type")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Patient has no prostate cancer with small cell histology")
                    .addFailGeneralMessages("Tumor type")
                    .build();
        }
    }
}
