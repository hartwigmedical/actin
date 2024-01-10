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
            val minimalDate = referenceDateProvider().date().minusMonths(VITAL_FUNCTION_MAX_AGE_MONTHS.toLong())
            val minMedianBloodPressure = functionInputResolver().createOneIntegerInput(function)
            HasSufficientBloodPressure(category, minMedianBloodPressure, minimalDate)
        }
    }

    private fun hasLimitedBloodPressureCreator(category: BloodPressureCategory): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minimalDate = referenceDateProvider().date().minusMonths(VITAL_FUNCTION_MAX_AGE_MONTHS.toLong())
            val maxMedianBloodPressure = functionInputResolver().createOneIntegerInput(function)
            HasLimitedBloodPressure(category, maxMedianBloodPressure, minimalDate)
        }
    }

    private fun hasSufficientPulseOximetryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minimalDate = referenceDateProvider().date().minusMonths(VITAL_FUNCTION_MAX_AGE_MONTHS.toLong())
            val minMedianPulseOximetry = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPulseOximetry(minMedianPulseOximetry, minimalDate)
        }
    }

    private fun hasRestingHeartRateWithinBoundsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minimalDate = referenceDateProvider().date().minusMonths(VITAL_FUNCTION_MAX_AGE_MONTHS.toLong())
            val input = functionInputResolver().createTwoDoublesInput(function)
            HasRestingHeartRateWithinBounds(input.double1(), input.double2(), minimalDate)
        }
    }

    private fun hasSufficientBodyWeightCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minBodyWeight = functionInputResolver().createOneDoubleInput(function)
            val minimalDate = referenceDateProvider().date().minusMonths(BODY_WEIGHT_MAX_AGE_MONHTS.toLong())
            HasSufficientBodyWeight(minBodyWeight, minimalDate)
        }
    }

    private fun hasLimitedBodyWeightCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxBodyWeight = functionInputResolver().createOneDoubleInput(function)
            val minimalDate = referenceDateProvider().date().minusMonths(BODY_WEIGHT_MAX_AGE_MONHTS.toLong())
            HasLimitedBodyWeight(maxBodyWeight, minimalDate)
        }
    }

    private fun hasBMIUpToLimitCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maximumBMI = functionInputResolver().createOneIntegerInput(function)
            val minimalDate = referenceDateProvider().date().minusMonths(BODY_WEIGHT_MAX_AGE_MONHTS.toLong())
            HasBMIUpToLimit(maximumBMI, minimalDate)
        }
    }

    companion object {
        private const val BODY_WEIGHT_MAX_AGE_MONHTS = 1
        private const val VITAL_FUNCTION_MAX_AGE_MONTHS = 1
    }
}