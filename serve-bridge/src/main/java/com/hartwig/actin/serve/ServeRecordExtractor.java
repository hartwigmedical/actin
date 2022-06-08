package com.hartwig.actin.serve;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.serve.datamodel.ImmutableServeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.serve.interpretation.ServeExtraction;
import com.hartwig.actin.serve.interpretation.ServeRules;
import com.hartwig.actin.serve.sort.ServeRecordComparator;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.composite.CompositeInput;
import com.hartwig.actin.treatment.input.composite.CompositeRules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ServeRecordExtractor {

    private ServeRecordExtractor() {
    }

    @NotNull
    public static Set<ServeRecord> extract(@NotNull List<Trial> trials) {
        Set<ServeRecord> records = Sets.newTreeSet(new ServeRecordComparator());

        for (Trial trial : trials) {
            String trialAcronym = trial.identification().acronym();
            records.addAll(extractFromEligibility(trialAcronym, null, trial.generalEligibility()));
            for (Cohort cohort : trial.cohorts()) {
                records.addAll(extractFromEligibility(trialAcronym, cohort.metadata().cohortId(), cohort.eligibility()));
            }
        }
        return records;
    }

    @NotNull
    private static Set<ServeRecord> extractFromEligibility(@NotNull String trial, @Nullable String cohort,
            @NotNull List<Eligibility> eligibilities) {
        Set<ServeRecord> records = Sets.newHashSet();

        for (Eligibility eligibility : eligibilities) {
            records.addAll(extractFromFunction(trial, cohort, eligibility.function(), true, true));
        }

        return records;
    }

    @NotNull
    private static Set<ServeRecord> extractFromFunction(@NotNull String trial, @Nullable String cohort,
            @NotNull EligibilityFunction function, boolean isAllowedToBeUsedAsInclusion, boolean isUsedAsInclusion) {
        Set<ServeRecord> records = Sets.newHashSet();

        if (CompositeRules.isComposite(function.rule())) {
            CompositeInput input = CompositeRules.inputsForCompositeRule(function.rule());
            if (input == CompositeInput.EXACTLY_1) {
                EligibilityFunction subFunction = FunctionInputResolver.createOneCompositeParameter(function);

                boolean isStillUsedAsInclusion = isUsedAsInclusion;
                boolean isStillAllowedToBeUsedAsInclusion = isAllowedToBeUsedAsInclusion;
                if (function.rule() == EligibilityRule.NOT) {
                    isStillUsedAsInclusion = !isUsedAsInclusion;
                } else if (function.rule() == EligibilityRule.WARN_IF) {
                    isStillAllowedToBeUsedAsInclusion = false;
                }
                records.addAll(extractFromFunction(trial, cohort, subFunction, isStillAllowedToBeUsedAsInclusion, isStillUsedAsInclusion));
            } else if (input == CompositeInput.AT_LEAST_2) {
                for (EligibilityFunction subFunction : FunctionInputResolver.createAtLeastTwoCompositeParameters(function)) {
                    records.addAll(extractFromFunction(trial, cohort, subFunction, isAllowedToBeUsedAsInclusion, isUsedAsInclusion));
                }
            } else {
                throw new IllegalStateException("Could not interpret composite input '" + input + "'");
            }
        } else if (ServeRules.isMolecular(function.rule())) {
            records.add(ImmutableServeRecord.builder()
                    .trial(trial)
                    .cohort(cohort)
                    .rule(function.rule())
                    .gene(ServeExtraction.gene(function))
                    .mutation(ServeExtraction.mutation(function))
                    .isUsedAsInclusion(isAllowedToBeUsedAsInclusion && isUsedAsInclusion)
                    .build());
        }

        return records;
    }
}
