package com.hartwig.actin.algo.evaluation.lifestyle

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.trial.datamodel.EligibilityRule

class LifestyleRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(EligibilityRule.IS_ABLE_AND_WILLING_TO_NOT_USE_CONTACT_LENSES to isWillingToNotUseContactLensesCreator())
    }

    private fun isWillingToNotUseContactLensesCreator(): FunctionCreator {
        return FunctionCreator { IsWillingToNotUseContactLenses() }
    }
}