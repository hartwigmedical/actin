package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate.Companion.createInterpreterForWashout
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.ManyDrugsParameter
import com.hartwig.actin.datamodel.trial.ManyMedicationCategoriesParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.StringParameter
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.medication.MedicationCategories

class WashoutRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    private val selector = MedicationSelector(MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date(), null))
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
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_DRUGS_X_WITHIN_Y_WEEKS to hasRecentlyReceivedAnyCancerTherapyButSomeDrugsCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES to hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(),
            EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES to hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(),
        )
    }

    private fun hasRecentlyReceivedCancerTherapyOfNamesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_DRUGS, Parameter.Type.INTEGER)
            val drugs = function.param<ManyDrugsParameter>(0).value
            val minWeeks = function.param<IntegerParameter>(1).value
            createReceivedCancerTherapyOfNameFunction(drugs, minWeeks)
        }
    }

    private fun hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_DRUGS, Parameter.Type.INTEGER, Parameter.Type.INTEGER)
            val drugs = function.param<ManyDrugsParameter>(0).value
            val minWeeks = function.param<IntegerParameter>(1).value
            createReceivedCancerTherapyOfNameFunction(drugs, minWeeks)
        }
    }

    private fun createReceivedCancerTherapyOfNameFunction(names: Set<Drug>, minWeeks: Int): EvaluationFunction {
        val (interpreter, minDate) = createInterpreterForWashout(minWeeks, null, referenceDateProvider().date())
        return HasRecentlyReceivedCancerTherapyWithDrug(names.toSet(), interpreter, minDate)
    }

    private fun hasRecentlyReceivedCancerTherapyOfCategoriesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_MEDICATION_CATEGORIES, Parameter.Type.INTEGER)
            val mappedCategories = mapMedicationCategories(function.param<ManyMedicationCategoriesParameter>(0).value)
            val minWeeks = function.param<IntegerParameter>(1).value
            createReceivedCancerTherapyOfCategoryFunction(mappedCategories, minWeeks)
        }
    }

    private fun hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.MANY_MEDICATION_CATEGORIES,
                Parameter.Type.INTEGER,
                Parameter.Type.INTEGER
            )
            val mappedCategories = mapMedicationCategories(function.param<ManyMedicationCategoriesParameter>(0).value)
            val minWeeks = function.param<IntegerParameter>(1).value
            createReceivedCancerTherapyOfCategoryFunction(mappedCategories, minWeeks)
        }
    }

    private fun createReceivedCancerTherapyOfCategoryFunction(
        mappedCategories: Map<String, Set<AtcLevel>>, minWeeks: Int
    ): EvaluationFunction {
        val (interpreter, minDate) = createInterpreterForWashout(minWeeks, null, referenceDateProvider().date())
        return HasRecentlyReceivedCancerTherapyOfCategory(mappedCategories, emptyMap(), emptySet(), interpreter, minDate)
    }

    private fun hasRecentlyReceivedTrialMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minWeeks = function.param<IntegerParameter>(0).value
            val maxStopDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong())
            HasRecentlyReceivedTrialMedication(selector, maxStopDate)
        }
    }

    private fun hasRecentlyReceivedTrialMedicationHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.INTEGER, Parameter.Type.INTEGER)
            val minWeeks = function.param<IntegerParameter>(0).value
            val maxStopDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong())
            HasRecentlyReceivedTrialMedication(selector, maxStopDate)
        }
    }

    private fun hasRecentlyReceivedRadiotherapyCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minWeeks = function.param<IntegerParameter>(0).value
            val maxStopDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong().minus(2))
            HasRecentlyReceivedRadiotherapy(maxStopDate.year, maxStopDate.monthValue, null)
        }
    }

    private fun hasRecentlyReceivedRadiotherapyToSomeBodyLocationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.STRING, Parameter.Type.INTEGER)
            val bodyLocation = function.param<StringParameter>(0).value
            val minWeeks = function.param<IntegerParameter>(1).value
            val maxStopDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong().minus(2))
            HasRecentlyReceivedRadiotherapy(maxStopDate.year, maxStopDate.monthValue, bodyLocation)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minWeeks = function.param<IntegerParameter>(0).value
            createReceivedAnyCancerTherapyFunction(minWeeks)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.INTEGER, Parameter.Type.INTEGER)
            val minWeeks = function.param<IntegerParameter>(0).value
            createReceivedAnyCancerTherapyFunction(minWeeks)
        }
    }

    private fun createReceivedAnyCancerTherapyFunction(minWeeks: Int): EvaluationFunction {
        val (interpreter, minDate) = createInterpreterForWashout(minWeeks, null, referenceDateProvider().date())
        return HasRecentlyReceivedCancerTherapyOfCategory(antiCancerCategories, emptyMap(), emptySet(), interpreter, minDate)
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_MEDICATION_CATEGORIES, Parameter.Type.INTEGER)
            val mappedCategories = mapMedicationCategories(function.param<ManyMedicationCategoriesParameter>(0).value)
            val minWeeks = function.param<IntegerParameter>(1).value
            createReceivedAnyCancerTherapyButSomeFunction(mappedCategories, emptySet(), minWeeks)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeDrugsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MANY_DRUGS, Parameter.Type.INTEGER)
            val drugs = function.param<ManyDrugsParameter>(0).value
            val minWeeks = function.param<IntegerParameter>(1).value
            createReceivedAnyCancerTherapyButSomeFunction(emptyMap(), drugs, minWeeks)
        }
    }

    private fun hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(
                Parameter.Type.MANY_MEDICATION_CATEGORIES,
                Parameter.Type.INTEGER,
                Parameter.Type.INTEGER
            )
            val mappedCategories = mapMedicationCategories(function.param<ManyMedicationCategoriesParameter>(0).value)
            val minWeeks = function.param<IntegerParameter>(1).value
            createReceivedAnyCancerTherapyButSomeFunction(mappedCategories, emptySet(), minWeeks)
        }
    }

    private fun createReceivedAnyCancerTherapyButSomeFunction(
        mappedIgnoredCategories: Map<String, Set<AtcLevel>>,
        drugsToIgnore: Set<Drug>,
        minWeeks: Int
    ): EvaluationFunction {
        val (interpreter, minDate) = createInterpreterForWashout(minWeeks, null, referenceDateProvider().date())
        return HasRecentlyReceivedCancerTherapyOfCategory(
            antiCancerCategories,
            mappedIgnoredCategories,
            drugsToIgnore,
            interpreter,
            minDate
        )
    }

    private fun mapMedicationCategories(categoryNames: List<String>): Map<String, Set<AtcLevel>> {
        return categoryNames.associate { category -> toMedicationCategoryMap(category) }
    }

    private fun toMedicationCategoryMap(category: String): Pair<String, Set<AtcLevel>> {
        throwExceptionIfAtcCategoryNotMapped(category)
        return categories.resolveCategoryName(category) to categories.resolve(category)
    }

    private fun throwExceptionIfAtcCategoryNotMapped(category: String) {
        val hasMapping = MedicationCategories.MEDICATION_CATEGORIES_TO_TREATMENT_CATEGORY.containsKey(category)
            || MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES.containsKey(category)
        if (MedicationCategories.ANTI_CANCER_ATC_CODES.any { category.startsWith(it) } && !hasMapping) {
            throw IllegalStateException("No treatment category or drug type mapping for ATC code $category")
        }
    }
}
