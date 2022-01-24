package com.hartwig.actin.serve;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ImmutableServeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.serve.interpretation.ServeRules;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.composite.CompositeInput;
import com.hartwig.actin.treatment.interpretation.composite.CompositeRules;

import org.jetbrains.annotations.NotNull;

public final class ServeRecordExtractor {

    private static final String FUSION_GENE_SEPARATOR = "-";

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
            records.add(toServeRecord(trialAcronym, function));
        }

        return records;
    }

    @NotNull
    private static ServeRecord toServeRecord(@NotNull String trialAcronym, @NotNull EligibilityFunction function) {
        String gene;
        String mutation = null;
        if (function.parameters().isEmpty()) {
            throw new IllegalStateException("Cannot convert function without parameters: " + function.rule());
        } else {
            gene = (String) function.parameters().get(0);

            if (function.parameters().size() > 1) {
                if (function.rule() == EligibilityRule.SPECIFIC_FUSION_OF_X_TO_Y) {
                    gene = gene + FUSION_GENE_SEPARATOR + function.parameters().get(1);
                } else if (function.rule() == EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y) {
                    mutation = (String) function.parameters().get(1);
                } else {
                    throw new IllegalStateException("Did not expect more than 1 param for " + function);
                }
            }
        }
        return ImmutableServeRecord.builder().trial(trialAcronym).rule(function.rule()).gene(gene).mutation(mutation).build();
    }
}
