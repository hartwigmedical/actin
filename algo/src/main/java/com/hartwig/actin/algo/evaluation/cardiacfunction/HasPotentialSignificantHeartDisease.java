package com.hartwig.actin.algo.evaluation.cardiacfunction;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.algo.othercondition.OtherConditionSelector;
import com.hartwig.actin.clinical.datamodel.ECG;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasPotentialSignificantHeartDisease implements EvaluationFunction {

    static final Set<String> HEART_DISEASE_DOIDS = Sets.newHashSet();
    static final Set<String> HEART_DISEASE_TERMS = Sets.newHashSet();

    static {
        HEART_DISEASE_DOIDS.add(DoidConstants.HEART_DISEASE_DOID);
        HEART_DISEASE_DOIDS.add(DoidConstants.HYPERTENSION_DOID);
        HEART_DISEASE_DOIDS.add(DoidConstants.CORONARY_ARTERY_DISEASE_DOID);

        HEART_DISEASE_TERMS.add("angina");
        HEART_DISEASE_TERMS.add("pacemaker");
    }

    @NotNull
    private final DoidModel doidModel;

    HasPotentialSignificantHeartDisease(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        ECG ecg = record.clinical().clinicalStatus().ecg();
        if (ecg != null && ecg.hasSigAberrationLatestECG()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has significant aberration on latest ECG")
                    .addPassGeneralMessages("Potential heart disease")
                    .build();
        }

        Set<String> heartConditions = Sets.newHashSet();
        for (PriorOtherCondition condition : OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            boolean hasHeartDiseaseDoid = false;
            for (String doid : condition.doids()) {
                if (isPotentialHearthDiseaseDoid(doid)) {
                    hasHeartDiseaseDoid = true;
                }
            }

            boolean hasHeartDiseaseTerm = isPotentiallyHeartDisease(condition.name());

            if (hasHeartDiseaseDoid || hasHeartDiseaseTerm) {
                heartConditions.add(condition.name());
            }
        }

        if (!heartConditions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Patient has " + Format.concat(heartConditions) + ", which classifies as potential heart disease")
                    .addPassGeneralMessages("Present " + Format.concat(heartConditions))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no potential significant heart disease")
                .addFailGeneralMessages("Potential heart disease")
                .build();
    }

    private static boolean isPotentiallyHeartDisease(@NotNull String name) {
        String lowerCaseName = name.toLowerCase();
        for (String heartDiseaseTerm : HEART_DISEASE_TERMS) {
            if (lowerCaseName.contains(heartDiseaseTerm.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPotentialHearthDiseaseDoid(@NotNull String doid) {
        Set<String> expanded = doidModel.doidWithParents(doid);
        for (String heartDiseaseDoid : HEART_DISEASE_DOIDS) {
            if (expanded.contains(heartDiseaseDoid)) {
                return true;
            }
        }
        return false;
    }
}
