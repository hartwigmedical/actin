package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class SurgeryRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_HAD_RECENT_SURGERY to hasHadRecentSurgeryCreator(),
            EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS to hasHadSurgeryInPastWeeksCreator(),
            EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_MONTHS to hasHadSurgeryInPastMonthsCreator(),
            EligibilityRule.HAS_PLANNED_SURGERY to hasPlannedSurgery()
        )
    }

    private fun hasHadRecentSurgeryCreator(): FunctionCreator {
        val evaluationDate = referenceDateProvider().date()
        val minDate = evaluationDate.minusMonths(2)
        return FunctionCreator { HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate) }
    }

    private fun hasHadSurgeryInPastWeeksCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val evaluationDate = referenceDateProvider().date()
            val maxAgeWeeks = functionInputResolver().createOneIntegerInput(function)
            val minDate = evaluationDate.minusWeeks(maxAgeWeeks.toLong()).plusWeeks(2)
            HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate)
        }
    }

    private fun hasHadSurgeryInPastMonthsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val evaluationDate = referenceDateProvider().date()
            val maxAgeMonths = functionInputResolver().createOneIntegerInput(function)
            val minDate = evaluationDate.minusMonths(maxAgeMonths.toLong())
            HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate)
        }
    }

    private fun hasPlannedSurgery(): FunctionCreator {
        val evaluationDate = referenceDateProvider().date()
        val minDate = evaluationDate.plusWeeks(4)
        return FunctionCreator { HasHadAnySurgeryAfterSpecificDate(minDate, minDate) }
    }
}