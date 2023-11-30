package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.medication.MedicationCategories
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class WashoutRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    private val selector: MedicationSelector
    private val categories: MedicationCategories

    init {
        val interpreter: MedicationStatusInterpreter = MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date())
        selector = MedicationSelector(interpreter)
        categories = MedicationCategories.create(atcTree())
    }

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS to hasRecentlyReceivedCancerTherapyOfNamesCreator(),
            EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS to hasRecentlyReceivedCancerTherapyOfCategoriesCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_TRIAL_MEDICATION_WITHIN_X_WEEKS to hasRecentlyReceivedTrialMedicationCreator(),
            EligibilityRule.HAS_RECEIVED_TRIAL_MEDICATION_WITHIN_X_WEEKS_Y_HALF_LIVES to hasRecentlyReceivedTrialMedicationHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS to hasRecentlyReceivedRadiotherapyCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS to hasRecentlyReceivedAnyCancerTherapyCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS to hasRecentlyReceivedAnyCancerTherapyButSomeCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES to hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(),
            EligibilityRule.WILL_REQUIRE_ANY_ANTICANCER_THERAPY_DURING_TRIAL to willRequireAnticancerTherapyCreator(),
        )
    }

    private fun hasRecentlyReceivedCancerTherapyOfNamesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsOneIntegerInput(function)
            createReceivedCancerTherapyOfNameFunction(input.strings(), input.integer())
        }
    }

    private fun hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsTwoIntegersInput(function)
            createReceivedCancerTherapyOfNameFunction(input.strings(), input.integer1())
        }
    }

    private fun createReceivedCancerTherapyOfNameFunction(names: List<String>, minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        return HasRecentlyReceivedCancerTherapyOfName(names.toSet(), interpreter)
    }

    private fun hasRecentlyReceivedCancerTherapyOfCategoriesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsOneIntegerInput(function)
            createReceivedCancerTherapyOfCategoryFunction(input.strings(), input.integer())
        }
    }

    private fun hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsTwoIntegersInput(function)
            createReceivedCancerTherapyOfCategoryFunction(input.strings(), input.integer1())
        }
    }

    private fun createReceivedCancerTherapyOfCategoryFunction(categoryInputs: List<String>, minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        val mappedCategories = categoryInputs.associateWith(categories::resolve)
        return HasRecentlyReceivedCancerTherapyOfCategory(mappedCategories, emptyMap(), interpreter)
    }

    private fun hasRecentlyReceivedTrialMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.toLong())
            HasRecentlyReceivedTrialMedication(selector, maxStopDate)
        }
    }

    private fun hasRecentlyReceivedTrialMedicationHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoIntegersInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.integer1().toLong())
            HasRecentlyReceivedTrialMedication(selector, maxStopDate)
        }
    }

    private fun hasRecentlyReceivedRadiotherapyCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.toLong().minus(2))
            HasRecentlyReceivedRadiotherapy(maxStopDate.year, maxStopDate.monthValue)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minWeeks = functionInputResolver().createOneIntegerInput(function)
            createReceivedAnyCancerTherapyFunction(minWeeks)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoIntegersInput(function)
            createReceivedAnyCancerTherapyFunction(input.integer1())
        }
    }

    private fun createReceivedAnyCancerTherapyFunction(minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        val antiCancerCategories = mapOf("Anticancer" to categories.resolve("Anticancer"))
        return HasRecentlyReceivedCancerTherapyOfCategory(antiCancerCategories, emptyMap(), interpreter)
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsOneIntegerInput(function)
            createReceivedAnyCancerTherapyButSomeFunction(input.strings(), input.integer())
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createManyStringsTwoIntegersInput(function)
            createReceivedAnyCancerTherapyButSomeFunction(input.strings(), input.integer1())
        }
    }

    private fun createReceivedAnyCancerTherapyButSomeFunction(categoriesToIgnore: List<String>, minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        val antiCancerCategories = mapOf("Anticancer" to categories.resolve("Anticancer"))
        val mappedIgnoredCategories = categoriesToIgnore.associateWith(categories::resolve)
        return HasRecentlyReceivedCancerTherapyOfCategory(antiCancerCategories, mappedIgnoredCategories, interpreter)
    }

    private fun willRequireAnticancerTherapyCreator(): FunctionCreator {
        return FunctionCreator { WillRequireAnticancerTherapy() }
    }

    private fun createInterpreterForWashout(inputWeeks: Int): MedicationStatusInterpreter {
        val minDate = referenceDateProvider().date().minusWeeks(inputWeeks.toLong()).plusWeeks(2)
        return MedicationStatusInterpreterOnEvaluationDate(minDate)
    }
}