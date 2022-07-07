package com.hartwig.actin.algo.evaluation.reproduction;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class ReproductionRuleMapper extends RuleMapper {

    public ReproductionRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_BREASTFEEDING, isBreastfeedingCreator());
        map.put(EligibilityRule.IS_PREGNANT, isPregnantCreator());
        map.put(EligibilityRule.USES_ADEQUATE_ANTICONCEPTION, canUseAdequateAnticonceptionCreator());
        map.put(EligibilityRule.ADHERES_TO_SPERM_OR_EGG_DONATION_PRESCRIPTIONS, willingToAdhereToDonationPrescriptionsCreator());

        return map;
    }

    @NotNull
    private FunctionCreator isBreastfeedingCreator() {
        return function -> new IsBreastfeeding();
    }

    @NotNull
    private FunctionCreator isPregnantCreator() {
        return function -> new IsPregnant();
    }

    @NotNull
    private FunctionCreator canUseAdequateAnticonceptionCreator() {
        return function -> new CanUseAdequateAnticonception();
    }

    @NotNull
    private FunctionCreator willingToAdhereToDonationPrescriptionsCreator() {
        return function -> new WillingToAdhereToDonationPrescriptions();
    }
}
