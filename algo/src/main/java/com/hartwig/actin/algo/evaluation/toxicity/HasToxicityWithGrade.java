package com.hartwig.actin.algo.evaluation.toxicity;

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

        Set<String> unresolvableToxicities = Sets.newHashSet();
        Set<String> toxicities = Sets.newHashSet();
        for (Toxicity toxicity : removeIgnored(record.clinical().toxicities())) {
            Integer grade = toxicity.grade();
            if (grade == null && toxicity.source() == ToxicitySource.QUESTIONNAIRE) {
                if (minGrade > DEFAULT_QUESTIONNAIRE_GRADE) {
                    hasUnresolvableQuestionnaireToxicities = true;
                    unresolvableToxicities.add(toxicity.name());
                }
                grade = DEFAULT_QUESTIONNAIRE_GRADE;
            }

            boolean gradeMatch = grade != null && grade >= minGrade;
            boolean nameMatch = nameFilter == null || toxicity.name().toLowerCase().contains(nameFilter.toLowerCase());
            if (gradeMatch && nameMatch) {
                toxicities.add(toxicity.name());
            }
        }

        if (!toxicities.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Toxicities with grade => " + minGrade + " found: " + Format.concat(toxicities))
                    .addPassGeneralMessages(Format.concat(toxicities))
                    .build();
        } else if (hasUnresolvableQuestionnaireToxicities) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("The exact grade (2, 3 or 4) is not known for all toxicities")
                    .addUndeterminedGeneralMessages(Format.concat(unresolvableToxicities))
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No toxicities found with grade " + minGrade + " or higher")
                .addFailGeneralMessages("No grade =>" + minGrade + " toxicities")
                .build();
    }

    @NotNull
    private List<Toxicity> removeIgnored(@NotNull List<Toxicity> toxicities) {
        List<Toxicity> filtered = Lists.newArrayList();
        for (Toxicity toxicity : toxicities) {
            boolean hasIgnoredName = false;
            for (String ignoreFilter : ignoreFilters) {
                if (toxicity.name().toLowerCase().contains(ignoreFilter.toLowerCase())) {
                    hasIgnoredName = true;
                }
            }

            if (!hasIgnoredName) {
                filtered.add(toxicity);
            }
        }
        return filtered;
    }
}
