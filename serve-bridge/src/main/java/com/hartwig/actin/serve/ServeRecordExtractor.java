package com.hartwig.actin.serve;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ImmutableServeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.serve.interpretation.ServeRules;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.composite.CompositeInput;
import com.hartwig.actin.treatment.interpretation.composite.CompositeRules;

import org.jetbrains.annotations.NotNull;

public final class ServeRecordExtractor {

    private ServeRecordExtractor() {
    }

    @NotNull
    public static List<ServeRecord> extract(@NotNull List<Trial> trials) {
        List<ServeRecord> records = Lists.newArrayList();

        for (Trial trial : trials) {
            String trialAcronym = trial.identification().acronym();
            records.addAll(extractFromEligibility(trialAcronym, trial.generalEligibility()));
            for (Cohort cohort : trial.cohorts()) {
                records.addAll(extractFromEligibility(trialAcronym, cohort.eligibility()));
            }
        }
        return records;
    }

    @NotNull
    private static List<ServeRecord> extractFromEligibility(@NotNull String trialAcronym, @NotNull List<Eligibility> eligibilities) {
        List<ServeRecord> records = Lists.newArrayList();

        for (Eligibility eligibility : eligibilities) {
            records.addAll(extractFromFunction(trialAcronym, eligibility.function()));
        }

        return records;
    }

    @NotNull
    private static List<ServeRecord> extractFromFunction(@NotNull String trialAcronym, @NotNull EligibilityFunction function) {
        List<ServeRecord> records = Lists.newArrayList();

        if (CompositeRules.isComposite(function.rule())) {
            CompositeInput input = CompositeRules.inputsForCompositeRule(function.rule());
            if (input == CompositeInput.EXACTLY_1) {
                EligibilityFunction subFunction = FunctionInputResolver.createOneCompositeParameter(function);
                records.addAll(extractFromFunction(trialAcronym, subFunction));
            } else if (input == CompositeInput.AT_LEAST_2) {
                for (EligibilityFunction subFunction : FunctionInputResolver.createAtLeastTwoCompositeParameters(function)) {
                    records.addAll(extractFromFunction(trialAcronym, subFunction));
                }
            } else {
                throw new IllegalStateException("Could not interpret composite input '" + input + "'");
            }
        } else if (ServeRules.isMolecular(function.rule())) {
            records.add(ImmutableServeRecord.builder()
                    .trial(trialAcronym)
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
