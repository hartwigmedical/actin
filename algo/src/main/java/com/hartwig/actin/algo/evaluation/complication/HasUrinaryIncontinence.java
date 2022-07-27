package com.hartwig.actin.algo.evaluation.complication;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.jetbrains.annotations.NotNull;

public class HasUrinaryIncontinence implements EvaluationFunction {

    private static final Set<List<String>> URINARY_INCONTINENCE_PATTERNS = Sets.newHashSet();

    static {
        URINARY_INCONTINENCE_PATTERNS.add(Lists.newArrayList("incontinence"));
        URINARY_INCONTINENCE_PATTERNS.add(Lists.newArrayList("bladder", "control"));
    }

    HasUrinaryIncontinence() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> urinaryIncontinences = Sets.newHashSet();
        if (record.clinical().complications() != null) {
            for (Complication complication : record.clinical().complications()) {
                if (isPotentialUrinaryIncontinence(complication.name())) {
                    urinaryIncontinences.add(complication.name());
                }
            }
        }

        if (!urinaryIncontinences.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has urinary incontinence " + Format.concat(urinaryIncontinences))
                    .addPassGeneralMessages(Format.concat(urinaryIncontinences))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient does not have urinary incontinence")
                .build();
    }

    private static boolean isPotentialUrinaryIncontinence(@NotNull String complication) {
        return PatternMatcher.isMatch(complication, URINARY_INCONTINENCE_PATTERNS);
    }
}
