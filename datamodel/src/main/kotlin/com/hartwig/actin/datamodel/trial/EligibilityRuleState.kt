package com.hartwig.actin.datamodel.trial

data class EligibilityRuleState(
    val eligibilityRule: EligibilityRule,
    val usedStatus: EligibilityRuleUsedStatus
) {
    companion object {

        fun used(eligibilityRule: EligibilityRule): EligibilityRuleState =
            EligibilityRuleState(eligibilityRule, EligibilityRuleUsedStatus.USED)

        fun unused(eligibilityRule: EligibilityRule): EligibilityRuleState =
            EligibilityRuleState(eligibilityRule, EligibilityRuleUsedStatus.UNUSED)
    }
}
