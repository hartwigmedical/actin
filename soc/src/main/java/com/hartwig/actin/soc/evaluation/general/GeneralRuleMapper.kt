package com.hartwig.actin.soc.evaluation.general

import com.hartwig.actin.soc.evaluation.FunctionCreator
import com.hartwig.actin.soc.evaluation.RuleMapper
import com.hartwig.actin.soc.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class GeneralRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD to hasAtLeastCertainAgeCreator(),
                EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X to hasMaximumWHOStatusCreator())
    }

    private fun hasAtLeastCertainAgeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minAge: Int = functionInputResolver().createOneIntegerInput(function)
            HasAtLeastCertainAge(referenceDateProvider().year(), minAge)
        }
    }

    private fun hasMaximumWHOStatusCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maximumWHO: Int = functionInputResolver().createOneIntegerInput(function)
            HasMaximumWHOStatus(maximumWHO)
        }
    }
}