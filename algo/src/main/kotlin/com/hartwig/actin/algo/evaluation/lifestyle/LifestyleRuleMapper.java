package com.hartwig.actin.algo.evaluation.lifestyle;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class LifestyleRuleMapper extends RuleMapper {

    public LifestyleRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_NOT_USE_CONTACT_LENSES, isWillingToNotUseContactLensesCreator());

        return map;
    }

    @NotNull
    private FunctionCreator isWillingToNotUseContactLensesCreator() {
        return function -> new IsWillingToNotUseContactLenses();
    }
}