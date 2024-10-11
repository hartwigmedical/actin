package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.medication.MedicationCategories

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
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_ANY_CYP to getsAnyCYPInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_CYP_X to getsCYPXInducingMedicationCreator(),
            EligibilityRule.HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS to hasRecentlyReceivedCYPXInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_CYP_X to getsCYPXInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_ANY_CYP to getsAnyCypInhibitingOrInducingMedication(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X to getsCYPXInhibitingOrInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_CYP_X to getsCYPSubstrateMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_PGP to getsPGPInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP to getsPGPInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_PGP to getsPGPSubstrateMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP to getsBCRPInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_BCRP to getsBCRPSubstrateMedicationCreator(),
            EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING to getsStableDosingAnticoagulantMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_HERBAL_MEDICATION to getsHerbalMedicationCreator(),
        )
    }

    private fun getsActiveMedicationWithConfiguredNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsMedicationOfName(selector, setOf(termToFind))
        }
    }

    private fun getsActiveMedicationWithCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val categoryInput = functionInputResolver().createOneMedicationCategoryInput(function)
            CurrentlyGetsMedicationOfAtcLevel(selector, categoryInput.categoryName, categoryInput.atcLevels)
        }
    }

    private fun hasRecentlyReceivedMedicationOfAtcLevelCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (categoryInput, integerInput) = functionInputResolver().createOneMedicationCategoryOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(integerInput.toLong())
            HasRecentlyReceivedMedicationOfAtcLevel(selector, categoryInput.categoryName, categoryInput.atcLevels, maxStopDate)
        }
    }

    private fun getsQTProlongatingMedicationCreator(): FunctionCreator {
        return { CurrentlyGetsQTProlongatingMedication(selector) }
    }

    private fun getsAnyCYPInducingMedicationCreator(): FunctionCreator {
        return { CurrentlyGetsAnyCypInducingMedication(selector) }
    }

    private fun getsCYPXInducingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneCypInput(function)
            CurrentlyGetsCypXInducingMedication(selector, termToFind.cyp)
        }
    }

    private fun hasRecentlyReceivedCYPXInducingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneCypOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasRecentlyReceivedCypXInducingMedication(selector, input.cyp, maxStopDate)
        }
    }

    private fun getsCYPXInhibitingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneCypInput(function)
            CurrentlyGetsCypXInhibitingMedication(selector, termToFind.cyp)
        }
    }

    private fun getsAnyCypInhibitingOrInducingMedication(): FunctionCreator {
        return { CurrentlyGetsAnyCypInhibitingOrInducingMedication(selector) }
    }

    private fun getsCYPXInhibitingOrInducingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneCypInput(function)
            CurrentlyGetsCypXInhibitingOrInducingMedication(selector, termToFind.cyp)
        }
    }

    private fun getsCYPSubstrateMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneCypInput(function)
            CurrentlyGetsCypXSubstrateMedication(selector, termToFind.cyp)
        }
    }

    private fun getsPGPInhibitingMedicationCreator(): FunctionCreator {
        return { CurrentlyGetsPGPInhibitingMedication() }
    }

    private fun getsPGPSubstrateMedicationCreator(): FunctionCreator {
        return { CurrentlyGetsPGPSubstrateMedication() }
    }

    private fun getsBCRPInhibitingMedicationCreator(): FunctionCreator {
        return { CurrentlyGetsBCRPInhibitingMedication() }
    }

    private fun getsBCRPSubstrateMedicationCreator(): FunctionCreator {
        return { CurrentlyGetsBCRPSubstrateMedication() }
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

    companion object {
        // Undetermined Cyp
        val UNDETERMINED_CYP = setOf("2J2")
    }
}