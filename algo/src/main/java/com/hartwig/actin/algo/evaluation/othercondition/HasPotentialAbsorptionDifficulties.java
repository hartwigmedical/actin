package com.hartwig.actin.algo.evaluation.othercondition;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.doid.DoidModel;

import org.jetbrains.annotations.NotNull;

public class HasPotentialAbsorptionDifficulties implements EvaluationFunction {

    static final String GASTROINTESTINAL_SYSTEM_DISEASE_DOID = "77";

    static final Set<String> COMPLICATIONS_CAUSING_ABSORPTION_DIFFICULTY = Sets.newHashSet();

    static final Set<String> TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY = Sets.newHashSet();

    static {
        COMPLICATIONS_CAUSING_ABSORPTION_DIFFICULTY.add("diarrhea");
        COMPLICATIONS_CAUSING_ABSORPTION_DIFFICULTY.add("nausea");
        COMPLICATIONS_CAUSING_ABSORPTION_DIFFICULTY.add("small bowel resection");
        COMPLICATIONS_CAUSING_ABSORPTION_DIFFICULTY.add("colectomy");
        COMPLICATIONS_CAUSING_ABSORPTION_DIFFICULTY.add("vomit");

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
        for (PriorOtherCondition priorOtherCondition : record.clinical().priorOtherConditions()) {
            for (String doid : priorOtherCondition.doids()) {
                if (doidModel.doidWithParents(doid).contains(GASTROINTESTINAL_SYSTEM_DISEASE_DOID)) {
                    return EvaluationFactory.unrecoverable()
                            .result(EvaluationResult.PASS)
                            .addPassSpecificMessages("Patient has potential absorption difficulties due to " + doidModel.resolveTermForDoid(doid))
                            .addPassGeneralMessages("Potential absorption difficulties")
                            .build();
                }
            }
        }

        if (record.clinical().complications() != null) {
            for (Complication complication : record.clinical().complications()) {
                for (String termToFind : COMPLICATIONS_CAUSING_ABSORPTION_DIFFICULTY) {
                    if (complication.name().toLowerCase().contains(termToFind.toLowerCase())) {
                        return EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.PASS)
                                .addPassSpecificMessages("Patient has potential absorption difficulties due to " + complication.name())
                                .addPassGeneralMessages("Potential absorption difficulties")
                                .build();
                    }
                }
            }
        }

        for (Toxicity toxicity : record.clinical().toxicities()) {
            if (toxicity.source() == ToxicitySource.QUESTIONNAIRE || (toxicity.grade() != null && toxicity.grade() >= 2)) {
                for (String termToFind : TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY) {
                    if (toxicity.name().toLowerCase().contains(termToFind.toLowerCase())) {
                        return EvaluationFactory.unrecoverable()
                                .result(EvaluationResult.PASS)
                                .addPassSpecificMessages("Patient has potential absorption difficulties due to " + toxicity.name())
                                .addPassGeneralMessages("Potential absorption difficulties")
                                .build();
                    }
                }
            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No potential reasons for absorption problems identified")
                .addFailGeneralMessages("No potential absorption difficulties identified")
                .build();
    }
}
