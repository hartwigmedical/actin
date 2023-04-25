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

public class HasOvarianCancerWithMucinousComponent implements EvaluationFunction {

    static final Set<String> OVARIAN_MUCINOUS_DOIDS = Sets.newHashSet();
    static final Set<String> OVARIAN_MUCINOUS_DOID_SET = Sets.newHashSet();

    static {
        OVARIAN_MUCINOUS_DOIDS.add(DoidConstants.OVARIAN_MUCINOUS_MALIGNANT_ADENOFIBROMA_DOID);
        OVARIAN_MUCINOUS_DOIDS.add(DoidConstants.OVARIAN_MUCINOUS_CYSTADENOFIBROMA_DOID);
        OVARIAN_MUCINOUS_DOIDS.add(DoidConstants.MUCINOUS_OVARIAN_CYSTADENOMA_DOID);
        OVARIAN_MUCINOUS_DOIDS.add(DoidConstants.OVARIAN_MUCINOUS_CYSTADENOCARCINOMA_DOID);
        OVARIAN_MUCINOUS_DOIDS.add(DoidConstants.OVARIAN_MUCINOUS_ADENOCARCINOMA_DOID);
        OVARIAN_MUCINOUS_DOIDS.add(DoidConstants.OVARIAN_MUCINOUS_NEOPLASM_DOID);
        OVARIAN_MUCINOUS_DOIDS.add(DoidConstants.OVARIAN_MUCINOUS_ADENOFIBROMA_DOID);
        OVARIAN_MUCINOUS_DOIDS.add(DoidConstants.OVARIAN_SEROMUCINOUS_CARCINOMA_DOID);

        OVARIAN_MUCINOUS_DOID_SET.add(DoidConstants.MUCINOUS_ADENOCARCINOMA_DOID);
        OVARIAN_MUCINOUS_DOID_SET.add(DoidConstants.OVARIAN_CANCER_DOID);
    }

    @NotNull
    private final DoidModel doidModel;

    HasOvarianCancerWithMucinousComponent(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> tumorDoids = record.clinical().tumor().doids();
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has ovarian cancer with mucinous component")
                    .addUndeterminedGeneralMessages("Undetermined ovarian mucinous cancer")
                    .build();
        }

        boolean isOvarianMucinousType = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, OVARIAN_MUCINOUS_DOIDS);

        boolean hasSpecificOvarianMucinousCombination =
                DoidEvaluationFunctions.isOfDoidCombinationType(tumorDoids, OVARIAN_MUCINOUS_DOID_SET);

        if (isOvarianMucinousType || hasSpecificOvarianMucinousCombination) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has ovarian cancer with mucinous component")
                    .addPassGeneralMessages("Tumor type")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have ovarian cancer with mucinous component")
                .addFailGeneralMessages("Tumor type")
                .build();
    }
}
