package com.hartwig.actin.treatment.trial;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.input.composite.CompositeRules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class EligibilityRuleUsageEvaluator {

    private static final Logger LOGGER = LogManager.getLogger(EligibilityRuleUsageEvaluator.class);

    private EligibilityRuleUsageEvaluator() {
    }

    public static void evaluate(@NotNull List<Trial> trials) {
        Set<EligibilityRule> unusedRules = Sets.newHashSet(EligibilityRule.values());

        for (Trial trial : trials) {
            unusedRules.removeAll(extractRules(trial.generalEligibility()));

            for (Cohort cohort : trial.cohorts()) {
                unusedRules.removeAll(extractRules(cohort.eligibility()));
            }
        }

        if (!unusedRules.isEmpty()) {
            LOGGER.warn(" Found {} unused eligibility rules.", unusedRules.size());
            for (EligibilityRule rule : unusedRules) {
                LOGGER.warn("  '{}' not used in any trial or cohort", rule.toString());
            }
        } else {
            LOGGER.info(" Found no unused eligibility rules.");
        }
    }

    @NotNull
    private static Collection<EligibilityRule> extractRules(@NotNull List<Eligibility> eligibilities) {
        Collection<EligibilityRule> rules = Lists.newArrayList();
        for (Eligibility eligibility : eligibilities) {
            rules.addAll(extract(eligibility.function()));
        }
        return rules;
    }

    @NotNull
    private static List<EligibilityRule> extract(@NotNull EligibilityFunction function) {
        List<EligibilityRule> rules = Lists.newArrayList(function.rule());
        if (CompositeRules.isComposite(function.rule())) {
            for (Object param : function.parameters()) {
                rules.addAll(extract((EligibilityFunction) param));
            }
        }
        return rules;
    }
}
