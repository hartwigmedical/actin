package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class VitalFunctionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X to hasSufficientBloodPressureCreator(BloodPressureCategory.SYSTOLIC),
            EligibilityRule.HAS_SBP_MMHG_OF_AT_MOST_X to hasLimitedBloodPressureCreator(BloodPressureCategory.SYSTOLIC),
            EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X to hasSufficientBloodPressureCreator(BloodPressureCategory.DIASTOLIC),
            EligibilityRule.HAS_DBP_MMHG_OF_AT_MOST_X to hasLimitedBloodPressureCreator(BloodPressureCategory.DIASTOLIC),
            EligibilityRule.HAS_PULSE_OXIMETRY_OF_AT_LEAST_X to hasSufficientPulseOximetryCreator(),
            EligibilityRule.HAS_RESTING_HEART_RATE_BETWEEN_X_AND_Y to hasRestingHeartRateWithinBoundsCreator(),
            EligibilityRule.HAS_BODY_WEIGHT_OF_AT_LEAST_X to hasSufficientBodyWeightCreator(),
            EligibilityRule.HAS_BODY_WEIGHT_OF_AT_MOST_X to hasLimitedBodyWeightCreator(),
            EligibilityRule.HAS_BMI_OF_AT_MOST_X to hasBMIUpToLimitCreator()
        )
    }

    private fun hasSufficientBloodPressureCreator(category: BloodPressureCategory): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minMedianBloodPressure = functionInputResolver().createOneDoubleInput(function)
            HasSufficientBloodPressure(category, minMedianBloodPressure)
        }
    }

    private fun hasLimitedBloodPressureCreator(category: BloodPressureCategory): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxMedianBloodPressure = functionInputResolver().createOneDoubleInput(function)
            HasLimitedBloodPressure(category, maxMedianBloodPressure)
        }
    }

    private fun hasSufficientPulseOximetryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minMedianPulseOximetry = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPulseOximetry(minMedianPulseOximetry)
        }
    }

    private fun hasRestingHeartRateWithinBoundsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoDoublesInput(function)
            HasRestingHeartRateWithinBounds(input.double1(), input.double2())
        }
    }

    private fun hasSufficientBodyWeightCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minBodyWeight = functionInputResolver().createOneDoubleInput(function)
            HasSufficientBodyWeight(minBodyWeight)
        }
    }

    private fun hasLimitedBodyWeightCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxBodyWeight = functionInputResolver().createOneDoubleInput(function)
            HasLimitedBodyWeight(maxBodyWeight)
        }
    }

    private fun hasBMIUpToLimitCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maximumBMI = functionInputResolver().createOneIntegerInput(function)
            HasBMIUpToLimit(maximumBMI)
        }
    }
}