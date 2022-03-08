package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasToxicityWithGrade implements EvaluationFunction {

    @VisibleForTesting
    static final int DEFAULT_QUESTIONNAIRE_GRADE = 2;

    private final int minGrade;
    @Nullable
    private final String nameFilter;
    @NotNull
    private final Set<String> ignoreFilters;

    HasToxicityWithGrade(final int minGrade, @Nullable final String nameFilter, @NotNull final Set<String> ignoreFilters) {
        this.minGrade = minGrade;
        this.nameFilter = nameFilter;
        this.ignoreFilters = ignoreFilters;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasUnresolvableQuestionnaireToxicities = false;

        Set<String> toxicities = Sets.newHashSet();
        for (Toxicity toxicity : removeIgnored(record.clinical().toxicities())) {
            Integer grade = toxicity.grade();
            if (grade == null && toxicity.source() == ToxicitySource.QUESTIONNAIRE) {
                if (minGrade > DEFAULT_QUESTIONNAIRE_GRADE) {
                    hasUnresolvableQuestionnaireToxicities = true;
                }
                grade = DEFAULT_QUESTIONNAIRE_GRADE;
            }

            boolean gradeMatch = grade != null && grade >= minGrade;
            boolean nameMatch = nameFilter == null || toxicity.name().contains(nameFilter);
            if (gradeMatch && nameMatch) {
                toxicities.add(toxicity.name());
            }
        }

        if (!toxicities.isEmpty()) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.PASS)
                    .addPassMessages("Toxicities with grade " + minGrade + " found: " + concat(toxicities))
                    .build();
        } else if (hasUnresolvableQuestionnaireToxicities) {
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Could not resolve the grade of all toxicities")
                    .build();
        }

        return ImmutableEvaluation.builder()
                .result(EvaluationResult.FAIL)
                .addFailMessages("No toxicities found with grade " + minGrade + " or higher")
                .build();
    }

    @NotNull
    private List<Toxicity> removeIgnored(@NotNull List<Toxicity> toxicities) {
        List<Toxicity> filtered = Lists.newArrayList();
        for (Toxicity toxicity : toxicities) {
            if (!stringIsPresentInSet(toxicity.name(), ignoreFilters)) {
                filtered.add(toxicity);
            }
        }
        return filtered;
    }

    private static boolean stringIsPresentInSet(@NotNull String string, @NotNull Set<String> set) {
        for (String entry : set) {
            if (string.contains(entry)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
