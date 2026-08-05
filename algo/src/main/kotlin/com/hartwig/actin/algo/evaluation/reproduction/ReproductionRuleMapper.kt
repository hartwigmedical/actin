package com.hartwig.actin.algo.evaluation.reproduction

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.trial.input.EligibilityRule

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
        return { IsBreastfeeding(evaluationLabels.reproduction) }
    }

    private fun isPregnantCreator(): FunctionCreator {
        return { IsPregnant(evaluationLabels.reproduction) }
    }

    private fun canUseAdequateAnticonceptionCreator(): FunctionCreator {
        return { CanUseAdequateAnticonception(evaluationLabels.reproduction) }
    }

    private fun willingToAdhereToDonationPrescriptionsCreator(): FunctionCreator {
        return { WillingToAdhereToDonationPrescriptions(evaluationLabels.reproduction) }
    }
}