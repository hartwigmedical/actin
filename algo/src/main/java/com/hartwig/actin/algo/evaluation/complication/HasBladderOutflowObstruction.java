package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.jetbrains.annotations.NotNull;

public class HasBladderOutflowObstruction implements EvaluationFunction {

    private static final Set<List<String>> BLADDER_OUTFLOW_OBSTRUCTION_PATTERNS = Sets.newHashSet();

    @VisibleForTesting
    static final String BLADDER_NECK_OBSTRUCTION_DOID = "13948";

    static {
        BLADDER_OUTFLOW_OBSTRUCTION_PATTERNS.add(Lists.newArrayList("bladder", "outflow"));
        BLADDER_OUTFLOW_OBSTRUCTION_PATTERNS.add(Lists.newArrayList("bladder", "outlet"));
        BLADDER_OUTFLOW_OBSTRUCTION_PATTERNS.add(Lists.newArrayList("bladder", "obstruction"));
        BLADDER_OUTFLOW_OBSTRUCTION_PATTERNS.add(Lists.newArrayList("bladder", "retention"));
    }

    HasBladderOutflowObstruction() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> bladderOutflowObstructions = Sets.newHashSet();
        for (Complication complication : record.clinical().complications()) {
            if (isPotentialBladderOutflowObstruction(complication.name())) {
                bladderOutflowObstructions.add(complication.name());
            }
        }

        if (!bladderOutflowObstructions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has bladder outflow obstruction " + Format.concat(bladderOutflowObstructions))
                    .addPassGeneralMessages(Format.concat(bladderOutflowObstructions))
                    .build();
        }

        Set<String> priorBladderConditions = Sets.newHashSet();
        for (PriorOtherCondition condition : record.clinical().priorOtherConditions()){
            for (String doid : condition.doids()) {
                if (doid.equals(BLADDER_NECK_OBSTRUCTION_DOID)) {
                    priorBladderConditions.add(condition.name());
                }
            }
        }

        if (!priorBladderConditions.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has prior condition " + Format.concat(priorBladderConditions))
                    .addPassGeneralMessages(Format.concat(priorBladderConditions))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have bladder outflow obstruction")
                .build();
    }

    private static boolean isPotentialBladderOutflowObstruction(@NotNull String complication) {
        return PatternMatcher.isMatch(complication, BLADDER_OUTFLOW_OBSTRUCTION_PATTERNS);
    }
}
