package com.hartwig.actin.treatment.interpretation.composite;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class CompositeRules {

    @VisibleForTesting
    static final Map<EligibilityRule, CompositeInput> COMPOSITE_RULE_MAPPING = Maps.newHashMap();

    static {
        COMPOSITE_RULE_MAPPING.put(EligibilityRule.AND, CompositeInput.AT_LEAST_2);
        COMPOSITE_RULE_MAPPING.put(EligibilityRule.OR, CompositeInput.AT_LEAST_2);
        COMPOSITE_RULE_MAPPING.put(EligibilityRule.NOT, CompositeInput.EXACTLY_1);
        COMPOSITE_RULE_MAPPING.put(EligibilityRule.WARN_IF, CompositeInput.EXACTLY_1);
    }

    private CompositeRules() {
    }

    public static boolean isComposite(@NotNull EligibilityRule rule) {
        return COMPOSITE_RULE_MAPPING.containsKey(rule);
    }

    @NotNull
    public static CompositeInput inputsForCompositeRule(@NotNull EligibilityRule compositeRule) {
        return COMPOSITE_RULE_MAPPING.get(compositeRule);
    }
}

