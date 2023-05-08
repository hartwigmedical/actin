package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class ReproductionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.IS_BREASTFEEDING to isBreastfeedingCreator(),
            EligibilityRule.IS_PREGNANT to isPregnantCreator(),
            EligibilityRule.USES_ADEQUATE_ANTICONCEPTION to canUseAdequateAnticonceptionCreator(),
            EligibilityRule.ADHERES_TO_SPERM_OR_EGG_DONATION_PRESCRIPTIONS to willingToAdhereToDonationPrescriptionsCreator()
        )
    }

    private fun isBreastfeedingCreator(): FunctionCreator {
        return FunctionCreator { IsBreastfeeding() }
    }

    private fun isPregnantCreator(): FunctionCreator {
        return FunctionCreator { IsPregnant() }
    }

    private fun canUseAdequateAnticonceptionCreator(): FunctionCreator {
        return FunctionCreator { CanUseAdequateAnticonception() }
    }

    private fun willingToAdhereToDonationPrescriptionsCreator(): FunctionCreator {
        return FunctionCreator { WillingToAdhereToDonationPrescriptions() }
    }
}