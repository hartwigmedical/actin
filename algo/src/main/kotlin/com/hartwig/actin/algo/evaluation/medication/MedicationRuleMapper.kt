package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.datamodel.trial.CypParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.ManyDrugInteractionTypesParameter
import com.hartwig.actin.datamodel.trial.ManyStringsParameter
import com.hartwig.actin.datamodel.trial.MedicationCategoryParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.StringParameter
import com.hartwig.actin.datamodel.trial.TransporterParameter
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.trial.input.single.OneMedicationCategory

class MedicationRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    private val selector: MedicationSelector =
        MedicationSelector(MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date(), null))
    private val categories: MedicationCategories = MedicationCategories.create(atcTree())

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION to getsActiveMedicationWithConfiguredNameCreator(),
            EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION to getsActiveMedicationWithCategoryCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORY_X_MEDICATION_WITHIN_Y_WEEKS to hasRecentlyReceivedMedicationOfAtcLevelCreator(),
            EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION to getsQTProlongatingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_CYP_X to getsCypXInducingMedicationCreator(),
            EligibilityRule.HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS to hasRecentlyReceivedCypXInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_CYP_X to getsCypXInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X to getsCypXInhibitingOrInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_CYP_X to getsCypSubstrateMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_WITH_CYP_INTERACTION_OF_ANY_TYPE_X to getsAnyCypMedicationOfTypesCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_TRANSPORTER_X to getsTransporterInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_TRANSPORTER_X to getsTransporterSubstrateMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OR_INHIBITING_ANY_NON_EVALUABLE_TRANSPORTER_X to
                    getsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationCreator(),
            EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING to getsStableDosingAnticoagulantMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_HERBAL_MEDICATION to getsHerbalMedicationCreator(),
        )
    }

    private fun getsActiveMedicationWithConfiguredNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = function.param<StringParameter>(0).value
            CurrentlyGetsMedicationOfName(selector, setOf(termToFind))
        }
    }

    private fun getsActiveMedicationWithCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MEDICATION_CATEGORY)
            val categoryInput = medicationCategory(function.param<MedicationCategoryParameter>(0).value)
            CurrentlyGetsMedicationOfAtcLevel(selector, categoryInput.categoryName, categoryInput.atcLevels)
        }
    }

    private fun hasRecentlyReceivedMedicationOfAtcLevelCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.MEDICATION_CATEGORY, Parameter.Type.INTEGER)
            val categoryInput = medicationCategory(function.param<MedicationCategoryParameter>(0).value)
            val minWeeks = function.param<IntegerParameter>(1).value
            val maxStopDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong())
            HasRecentlyReceivedMedicationOfAtcLevel(selector, categoryInput.categoryName, categoryInput.atcLevels, maxStopDate)
        }
    }

    private fun getsQTProlongatingMedicationCreator(): FunctionCreator {
        return { CurrentlyGetsQTProlongatingMedication(selector) }
    }

    private fun getsCypXInducingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val cyp = function.param<CypParameter>(0).value
            CurrentlyGetsCypXInducingMedication(selector, MedicationUtil.extractCypString(cyp))
        }
    }

    private fun hasRecentlyReceivedCypXInducingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.CYP, Parameter.Type.INTEGER)
            val cyp = function.param<CypParameter>(0).value
            val minWeeks = function.param<IntegerParameter>(1).value
            val maxStopDate = referenceDateProvider().date().minusWeeks(minWeeks.toLong())
            HasRecentlyReceivedCypXInducingMedication(selector, MedicationUtil.extractCypString(cyp), maxStopDate)
        }
    }

    private fun getsCypXInhibitingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val cyp = function.param<CypParameter>(0).value
            CurrentlyGetsCypXInhibitingMedication(selector, MedicationUtil.extractCypString(cyp))
        }
    }

    private fun getsCypXInhibitingOrInducingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val cyp = function.param<CypParameter>(0).value
            CurrentlyGetsCypXInhibitingOrInducingMedication(selector, MedicationUtil.extractCypString(cyp))
        }
    }

    private fun getsCypSubstrateMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val cyp = function.param<CypParameter>(0).value
            CurrentlyGetsCypXSubstrateMedication(selector, MedicationUtil.extractCypString(cyp))
        }
    }

    private fun getsAnyCypMedicationOfTypesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val types = function.param<ManyDrugInteractionTypesParameter>(0).value
            CurrentlyGetsAnyCypMedicationOfTypes(selector, types)
        }
    }

    private fun getsTransporterInhibitingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = function.param<TransporterParameter>(0).value.toString()
            CurrentlyGetsTransporterInteractingMedication(selector, termToFind, DrugInteraction.Type.INHIBITOR)
        }
    }

    private fun getsTransporterSubstrateMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = function.param<TransporterParameter>(0).value.toString()
            CurrentlyGetsTransporterInteractingMedication(selector, termToFind, DrugInteraction.Type.SUBSTRATE)
        }
    }

    private fun getsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val types = function.param<ManyStringsParameter>(0).value
            CurrentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedication(selector, types)
        }
    }

    private fun getsStableDosingAnticoagulantMedicationCreator(): FunctionCreator {
        val categoryNameInput = "Anticoagulants"
        return {
            CurrentlyGetsStableMedicationOfCategory(
                selector,
                mapOf(categoryNameInput to categories.resolve(categoryNameInput))
            )
        }
    }

    private fun getsHerbalMedicationCreator(): FunctionCreator {
        return { CurrentlyGetsHerbalMedication(selector) }
    }

    private fun medicationCategory(categoryName: String): OneMedicationCategory {
        throwExceptionIfAtcCategoryNotMapped(categoryName)
        return OneMedicationCategory(categoryName, categories.resolve(categoryName))
    }

    private fun throwExceptionIfAtcCategoryNotMapped(category: String) {
        val hasMapping = MedicationCategories.MEDICATION_CATEGORIES_TO_TREATMENT_CATEGORY.containsKey(category)
                || MedicationCategories.MEDICATION_CATEGORIES_TO_DRUG_TYPES.containsKey(category)
        if (MedicationCategories.ANTI_CANCER_ATC_CODES.any { category.startsWith(it) } && !hasMapping) {
            throw IllegalStateException("No treatment category or drug type mapping for ATC code $category")
        }
    }
}
