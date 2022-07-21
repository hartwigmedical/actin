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

public class HasOvarianCancerWithMucinousComponent implements EvaluationFunction {

    static final Set<String> OVARIAN_MUCINOUS_DOIDS = Sets.newHashSet();
    static final Set<Set<String>> OVARIAN_MUCINOUS_DOID_SETS = Sets.newHashSet();

    static {
        OVARIAN_MUCINOUS_DOIDS.add("6278"); // ovarian mucinous malignant adenofibroma
        OVARIAN_MUCINOUS_DOIDS.add("7013"); // ovarian mucinous cystadenofibroma
        OVARIAN_MUCINOUS_DOIDS.add("3267"); // mucinous ovarian cystadenoma
        OVARIAN_MUCINOUS_DOIDS.add("3604"); // ovarian mucinous cystadenocarcinoma
        OVARIAN_MUCINOUS_DOIDS.add("3606"); // ovarian mucinous adenocarcinoma
        OVARIAN_MUCINOUS_DOIDS.add("6067"); // ovarian mucinous neoplasm
        OVARIAN_MUCINOUS_DOIDS.add("6469"); // ovarian mucinous adenofibroma
        OVARIAN_MUCINOUS_DOIDS.add("6898"); // ovarian seromucinous carcinoma

        Set<String> ovarianMucinousSet = Sets.newHashSet();
        ovarianMucinousSet.add("3030"); // mucinous adenocarcinoma
        ovarianMucinousSet.add("2394"); // ovarian cancer
        OVARIAN_MUCINOUS_DOID_SETS.add(ovarianMucinousSet);
    }

    @NotNull
    private final DoidModel doidModel;

    HasOvarianCancerWithMucinousComponent(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> patientDoids = record.clinical().tumor().doids();
        if (patientDoids == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine whether patient has ovarian cancer with mucinous component")
                    .addUndeterminedGeneralMessages("Ovarian mucinous cancer")
                    .build();
        }

        boolean isOvarianMucinousType = DoidEvaluationFunctions.hasDoidOfCertainType(doidModel,
                record.clinical().tumor(),
                OVARIAN_MUCINOUS_DOIDS,
                Sets.newHashSet());

        boolean hasSpecificOvarianMucinousCombination =
                DoidEvaluationFunctions.hasSpecificCombinationOfDoids(patientDoids, OVARIAN_MUCINOUS_DOID_SETS);

        if (isOvarianMucinousType || hasSpecificOvarianMucinousCombination) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has ovarian cancer with mucinous component")
                    .addPassGeneralMessages("Ovarian mucinous cancer")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have ovarian cancer with mucinous component")
                .addFailGeneralMessages("Ovarian mucinous cancer")
                .build();
    }
}
