package com.hartwig.actin.serve;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ImmutableServeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.serve.interpretation.MolecularRules;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.interpretation.CompositeInput;
import com.hartwig.actin.treatment.interpretation.CompositeRules;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ServeRecordExtractor {

    private ServeRecordExtractor() {
    }

    @NotNull
    public static List<ServeRecord> extract(@NotNull List<Trial> trials) {
        List<ServeRecord> records = Lists.newArrayList();

        for (Trial trial : trials) {
            String trialId = trial.identification().trialId();
            records.addAll(extractFromEligibility(trialId, null, trial.generalEligibility()));
            for (Cohort cohort : trial.cohorts()) {
                records.addAll(extractFromEligibility(trialId, cohort.metadata().cohortId(), cohort.eligibility()));
            }
        }
        return records;
    }

    @NotNull
    private static List<ServeRecord> extractFromEligibility(@NotNull String trialId, @Nullable String cohortId,
            @NotNull List<Eligibility> eligibilities) {
        List<ServeRecord> records = Lists.newArrayList();

        for (Eligibility eligibility : eligibilities) {
            records.addAll(extractFromFunction(trialId, cohortId, eligibility.function()));
        }

        return records;
    }

    @NotNull
    private static List<ServeRecord> extractFromFunction(@NotNull String trialId, @Nullable String cohortId,
            @NotNull EligibilityFunction function) {
        List<ServeRecord> records = Lists.newArrayList();

        if (CompositeRules.isComposite(function.rule())) {
            CompositeInput input = CompositeRules.inputsForCompositeRule(function.rule());
            if (input == CompositeInput.MAXIMUM_1) {
                EligibilityFunction subFunction = EligibilityParameterResolver.createOneCompositeParameter(function);
                records.addAll(extractFromFunction(trialId, cohortId, subFunction));
            } else if (input == CompositeInput.AT_LEAST_2) {
                for (EligibilityFunction subFunction : EligibilityParameterResolver.createAtLeastTwoCompositeParameters(function)) {
                    records.addAll(extractFromFunction(trialId, cohortId, subFunction));
                }
            } else {
                throw new IllegalStateException("Could not interpret composite input '" + input + "'");
            }
        } else if (MolecularRules.isMolecular(function.rule())) {
            records.add(ImmutableServeRecord.builder()
                    .trialId(trialId)
                    .cohortId(cohortId)
                    .rule(function.rule())
                    .parameters(toStrings(function.parameters()))
                    .build());
        }

        return records;
    }

    @NotNull
    private static List<String> toStrings(@NotNull List<Object> objects) {
        List<String> strings = Lists.newArrayList();
        for (Object object : objects) {
            strings.add((String) object);
        }
        return strings;
    }
}
