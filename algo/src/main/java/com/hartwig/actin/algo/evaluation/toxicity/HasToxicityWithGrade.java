package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
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
                return EvaluationFactory.create(EvaluationResult.PASS);
            }
        }

        EvaluationResult result = hasUnresolvableQuestionnaireToxicities ? EvaluationResult.UNDETERMINED : EvaluationResult.FAIL;
        return EvaluationFactory.create(result);
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
}
