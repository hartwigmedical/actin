package com.hartwig.actin.algo.evaluation.othercondition;

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
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasPotentialAbsorptionDifficulties implements EvaluationFunction {

    static final String GASTROINTESTINAL_DISORDER_CATEGORY = "gastrointestinal disorder";

    static final Set<String> TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY = Sets.newHashSet();

    static {
        TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY.add("diarrhea");
        TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY.add("nausea");
        TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY.add("vomit");
    }

    @NotNull
    private final DoidModel doidModel;

    HasPotentialAbsorptionDifficulties(@NotNull final DoidModel doidModel) {
        this.doidModel = doidModel;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> conditions = Sets.newHashSet();
        for (PriorOtherCondition condition : OtherConditionSelector.selectClinicallyRelevant(record.clinical().priorOtherConditions())) {
            for (String doid : condition.doids()) {
                if (doidModel.doidWithParents(doid).contains(DoidConstants.GASTROINTESTINAL_SYSTEM_DISEASE_DOID)) {
                    conditions.add(doidModel.resolveTermForDoid(doid));
                }
            }
        }

        if (!conditions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has potential absorption difficulties due to " + Format.concat(conditions))
                    .addPassGeneralMessages("Potential absorption difficulties: " + Format.concat(conditions))
                    .build();
        }

        Set<String> complications = Sets.newHashSet();
        if (record.clinical().complications() != null) {
            for (Complication complication : record.clinical().complications()) {
                if (isOfCategory(complication, GASTROINTESTINAL_DISORDER_CATEGORY)) {
                    complications.add(complication.name());
                }
            }
        }

        if (!complications.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has potential absorption difficulties due to " + Format.concat(complications))
                    .addPassGeneralMessages("Potential absorption difficulties: " + Format.concat(complications))
                    .build();
        }

        Set<String> toxicities = Sets.newHashSet();
        for (Toxicity toxicity : record.clinical().toxicities()) {
            if (toxicity.source() == ToxicitySource.QUESTIONNAIRE || (toxicity.grade() != null && toxicity.grade() >= 2)) {
                for (String termToFind : TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY) {
                    if (toxicity.name().toLowerCase().contains(termToFind.toLowerCase())) {
                        toxicities.add(toxicity.name());
                    }
                }
            }
        }

        if (!toxicities.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has potential absorption difficulties due to " + Format.concat(toxicities))
                    .addPassGeneralMessages("Potential absorption difficulties: " + Format.concat(toxicities))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No potential reasons for absorption problems identified")
                .addFailGeneralMessages("No potential absorption difficulties identified")
                .build();
    }

    private static boolean isOfCategory(@NotNull Complication complication, @NotNull String categoryToFind) {
        for (String category : complication.categories()) {
            if (category.toLowerCase().contains(categoryToFind.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
