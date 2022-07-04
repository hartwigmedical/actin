package com.hartwig.actin.algo.evaluation.tumor;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasSolidPrimaryTumor implements EvaluationFunction {

    static final String CANCER_DOID = "162";
    static final Set<String> NON_SOLID_CANCER_DOIDS = Sets.newHashSet();
    static final Set<String> WARN_SOLID_CANCER_DOIDS = Sets.newHashSet();

    static {
        NON_SOLID_CANCER_DOIDS.add("1240"); // leukemia
        NON_SOLID_CANCER_DOIDS.add("712"); // refractory hematologic cancer
        NON_SOLID_CANCER_DOIDS.add("4960"); // bone marrow cancer

        WARN_SOLID_CANCER_DOIDS.add("2531"); // hematologic cancer
    }

    @NotNull
    private final DoidModel doidModel;

    HasSolidPrimaryTumor(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> doids = record.clinical().tumor().doids();

        if (doids == null || doids.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No tumor location/type configured for patient, unknown if solid primary tumor")
                    .addUndeterminedGeneralMessages("Unconfigured tumor location/type")
                    .build();
        }

        boolean allDoidsAreCancer = true;
        boolean hasAtLeastOneNonSolidDoid = false;
        boolean hasAtLeastOneWarnSolidDoid = false;
        for (String doid : doids) {
            Set<String> doidTree = doidModel.doidWithParents(doid);
            if (!doidTree.contains(CANCER_DOID)) {
                allDoidsAreCancer = false;
            }

            for (String nonSolidCancer : NON_SOLID_CANCER_DOIDS) {
                if (doidTree.contains(nonSolidCancer)) {
                    hasAtLeastOneNonSolidDoid = true;
                    break;
                }
            }

            for (String warnSolidCancer : WARN_SOLID_CANCER_DOIDS) {
                if (doidTree.contains(warnSolidCancer)) {
                    hasAtLeastOneWarnSolidDoid = true;
                    break;
                }
            }
        }

        if (allDoidsAreCancer && !hasAtLeastOneNonSolidDoid) {
            if (hasAtLeastOneWarnSolidDoid) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.WARN)
                        .addWarnSpecificMessages("Patient has potentially non-solid primary tumor")
                        .addWarnGeneralMessages("Tumor type")
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("Patient has solid primary tumor")
                        .addPassGeneralMessages("Tumor type")
                        .build();
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has non-solid primary tumor")
                .addFailGeneralMessages("Tumor type")
                .build();
    }
}
