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

import org.jetbrains.annotations.NotNull;

public class HasSolidPrimaryTumorIncludingLymphoma implements EvaluationFunction {

    static final String CANCER_DOID = "162";
    static final Set<String> NON_SOLID_CANCER_DOIDS = Sets.newHashSet();
    static final Set<String> WARN_SOLID_CANCER_DOIDS = Sets.newHashSet();

    static {
        NON_SOLID_CANCER_DOIDS.add("1240"); // leukemia
        NON_SOLID_CANCER_DOIDS.add("712"); // refractory hematologic cancer
        NON_SOLID_CANCER_DOIDS.add("4960"); // bone marrow cancer

        WARN_SOLID_CANCER_DOIDS.add("5772"); // central nervous system hematologic cancer
        WARN_SOLID_CANCER_DOIDS.add("3282"); // dendritic cell thymoma
        WARN_SOLID_CANCER_DOIDS.add("5621"); // histiocytic and denditric cell cancer
        WARN_SOLID_CANCER_DOIDS.add("3664"); // mast cell neoplasm
        WARN_SOLID_CANCER_DOIDS.add("8683"); // myeloid sarcoma
    }

    @NotNull
    private final DoidModel doidModel;

    HasSolidPrimaryTumorIncludingLymphoma(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> doids = record.clinical().tumor().doids();

        if (doids == null || doids.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "No tumor location/type configured for patient, unknown if solid primary tumor or lymphoma")
                    .addUndeterminedGeneralMessages("Unconfigured tumor location/type")
                    .build();
        }

        EvaluationResult result =
                DoidEvaluationFunctions.evaluate(doidModel, doids, CANCER_DOID, NON_SOLID_CANCER_DOIDS, WARN_SOLID_CANCER_DOIDS);

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has non-solid primary tumor");
            builder.addFailGeneralMessages("Tumor type");
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Patient has potentially non-solid primary tumor");
            builder.addWarnGeneralMessages("Tumor type");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has solid primary tumor or lymphoma");
            builder.addPassGeneralMessages("Tumor type");
        }

        return builder.build();
    }
}
