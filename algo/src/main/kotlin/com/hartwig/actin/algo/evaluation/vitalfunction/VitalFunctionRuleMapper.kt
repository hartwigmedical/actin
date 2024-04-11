package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import java.time.LocalDate

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
            val minMedianBloodPressure = functionInputResolver().createOneIntegerInput(function)
            HasSufficientBloodPressure(category, minMedianBloodPressure, minimumDateForVitalFunction())
        }
    }

    private fun hasLimitedBloodPressureCreator(category: BloodPressureCategory): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxMedianBloodPressure = functionInputResolver().createOneIntegerInput(function)
            HasLimitedBloodPressure(category, maxMedianBloodPressure, minimumDateForVitalFunction())
        }
    }

    private fun hasSufficientPulseOximetryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minMedianPulseOximetry = functionInputResolver().createOneDoubleInput(function)
            HasSufficientPulseOximetry(minMedianPulseOximetry, minimumDateForVitalFunction())
        }
    }

    private fun hasRestingHeartRateWithinBoundsCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoDoublesInput(function)
            HasRestingHeartRateWithinBounds(input.double1, input.double2, minimumDateForVitalFunction())
        }
    }

    private fun hasSufficientBodyWeightCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minBodyWeight = functionInputResolver().createOneDoubleInput(function)
            HasSufficientBodyWeight(minBodyWeight, minimumDateForBodyWeight())
        }
    }

    private fun hasLimitedBodyWeightCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maxBodyWeight = functionInputResolver().createOneDoubleInput(function)
            HasLimitedBodyWeight(maxBodyWeight, minimumDateForBodyWeight())
        }
    }

    private fun hasBMIUpToLimitCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maximumBMI = functionInputResolver().createOneIntegerInput(function)
            HasBMIUpToLimit(maximumBMI, minimumDateForBodyWeight())
        }
    }

    private fun minimumDateForVitalFunction(): LocalDate {
        return referenceDateProvider().date().minusMonths(VITAL_FUNCTION_MAX_AGE_MONTHS.toLong())
    }

    private fun minimumDateForBodyWeight(): LocalDate {
        return referenceDateProvider().date().minusMonths(BODY_WEIGHT_MAX_AGE_MONTHS.toLong())
    }

    companion object {
        private const val BODY_WEIGHT_MAX_AGE_MONTHS = 2
        private const val VITAL_FUNCTION_MAX_AGE_MONTHS = 1
        const val BODY_WEIGHT_NEGATIVE_MARGIN_OF_ERROR = 0.975
        const val BODY_WEIGHT_POSITIVE_MARGIN_OF_ERROR = 1.025
        const val VITAL_FUNCTION_NEGATIVE_MARGIN_OF_ERROR = 0.95
        const val VITAL_FUNCTION_POSITIVE_MARGIN_OF_ERROR = 1.05
    }
}