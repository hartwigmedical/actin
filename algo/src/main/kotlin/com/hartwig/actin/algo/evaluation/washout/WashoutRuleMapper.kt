package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.medication.MedicationCategories

class WashoutRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    private val selector = MedicationSelector(MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date()))
    private val categories = MedicationCategories.create(atcTree())
    private val antiCancerCategories = mapOf("Anticancer" to categories.resolve("Anticancer"))

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS to hasRecentlyReceivedCancerTherapyOfNamesCreator(),
            EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS to hasRecentlyReceivedCancerTherapyOfCategoriesCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_TRIAL_MEDICATION_WITHIN_X_WEEKS to hasRecentlyReceivedTrialMedicationCreator(),
            EligibilityRule.HAS_RECEIVED_TRIAL_MEDICATION_WITHIN_X_WEEKS_Y_HALF_LIVES to hasRecentlyReceivedTrialMedicationHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS to hasRecentlyReceivedRadiotherapyCreator(),
            EligibilityRule.HAS_HAD_RADIOTHERAPY_TO_BODY_LOCATION_X_WITHIN_Y_WEEKS to hasRecentlyReceivedRadiotherapyToSomeBodyLocationCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS to hasRecentlyReceivedAnyCancerTherapyCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS to hasRecentlyReceivedAnyCancerTherapyButSomeCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES to hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(),
        )
    }

    private fun hasRecentlyReceivedCancerTherapyOfNamesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManyDrugsOneIntegerInput(function)
            createReceivedCancerTherapyOfNameFunction(input.drugs, input.integer)
        }
    }

    private fun hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createManyDrugsTwoIntegersInput(function)
            createReceivedCancerTherapyOfNameFunction(input.drugs, input.integer)
        }
    }

    private fun createReceivedCancerTherapyOfNameFunction(names: Set<Drug>, minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        val minDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong())
        return HasRecentlyReceivedCancerTherapyWithDrug(names.toSet(), interpreter, minDate)
    }

    private fun hasRecentlyReceivedCancerTherapyOfCategoriesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (mappedCategories, minWeeks) = functionInputResolver().createManyMedicationCategoriesOneIntegerInput(function)
            createReceivedCancerTherapyOfCategoryFunction(mappedCategories, minWeeks)
        }
    }

    private fun hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (mappedCategories, minWeeks, _) = functionInputResolver().createManyMedicationCategoriesTwoIntegersInput(function)
            createReceivedCancerTherapyOfCategoryFunction(mappedCategories, minWeeks)
        }
    }

    private fun createReceivedCancerTherapyOfCategoryFunction(
        mappedCategories: Map<String, Set<AtcLevel>>, minWeeks: Int
    ): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        val minDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong())
        return HasRecentlyReceivedCancerTherapyOfCategory(mappedCategories, emptyMap(), interpreter, minDate)
    }

    private fun hasRecentlyReceivedTrialMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.toLong())
            HasRecentlyReceivedTrialMedication(selector, maxStopDate)
        }
    }

    private fun hasRecentlyReceivedTrialMedicationHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoIntegersInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.integer1.toLong())
            HasRecentlyReceivedTrialMedication(selector, maxStopDate)
        }
    }

    private fun hasRecentlyReceivedRadiotherapyCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.toLong().minus(2))
            HasRecentlyReceivedRadiotherapy(maxStopDate.year, maxStopDate.monthValue, null)
        }
    }

    private fun hasRecentlyReceivedRadiotherapyToSomeBodyLocationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.integer.toLong().minus(2))
            HasRecentlyReceivedRadiotherapy(maxStopDate.year, maxStopDate.monthValue, input.string)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minWeeks = functionInputResolver().createOneIntegerInput(function)
            createReceivedAnyCancerTherapyFunction(minWeeks)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createTwoIntegersInput(function)
            createReceivedAnyCancerTherapyFunction(input.integer1)
        }
    }

    private fun createReceivedAnyCancerTherapyFunction(minWeeks: Int): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        val minDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong())
        return HasRecentlyReceivedCancerTherapyOfCategory(antiCancerCategories, emptyMap(), interpreter, minDate)
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (mappedCategories, minWeeks) = functionInputResolver().createManyMedicationCategoriesOneIntegerInput(function)
            createReceivedAnyCancerTherapyButSomeFunction(mappedCategories, minWeeks)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (mappedCategories, minWeeks, _) = functionInputResolver().createManyMedicationCategoriesTwoIntegersInput(function)
            createReceivedAnyCancerTherapyButSomeFunction(mappedCategories, minWeeks)
        }
    }

    private fun createReceivedAnyCancerTherapyButSomeFunction(
        mappedIgnoredCategories: Map<String, Set<AtcLevel>>,
        minWeeks: Int
    ): EvaluationFunction {
        val interpreter = createInterpreterForWashout(minWeeks)
        val minDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong())
        return HasRecentlyReceivedCancerTherapyOfCategory(antiCancerCategories, mappedIgnoredCategories, interpreter, minDate)
    }

    fun createInterpreterForWashout(inputWeeks: Int): MedicationStatusInterpreter {
        val minDate = referenceDateProvider().date().minusWeeks(inputWeeks.toLong()).plusWeeks(2)
        return MedicationStatusInterpreterOnEvaluationDate(minDate)
    }
}