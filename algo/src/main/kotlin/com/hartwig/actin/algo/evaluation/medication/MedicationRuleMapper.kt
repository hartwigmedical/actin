package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.clinical.Cyp
import com.hartwig.actin.datamodel.clinical.DrugInteraction
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
            CurrentlyGetsCypXInducingMedication(selector, extractCypString(termToFind))
        }
    }

    private fun hasRecentlyReceivedCYPXInducingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneCypOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.integer.toLong())
            HasRecentlyReceivedCypXInducingMedication(selector, extractCypString(input.cyp), maxStopDate)
        }
    }

    private fun getsCYPXInhibitingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneCypInput(function)
            CurrentlyGetsCypXInhibitingMedication(selector, extractCypString(termToFind))
        }
    }

    private fun getsAnyCypInhibitingOrInducingMedication(): FunctionCreator {
        return { CurrentlyGetsAnyCypInhibitingOrInducingMedication(selector) }
    }

    private fun getsCYPXInhibitingOrInducingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneCypInput(function)
            CurrentlyGetsCypXInhibitingOrInducingMedication(selector, extractCypString(termToFind))
        }
    }

    private fun getsCYPSubstrateMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneCypInput(function)
            CurrentlyGetsCypXSubstrateMedication(selector, extractCypString(termToFind))
        }
    }

    private fun getsTransporterInhibitingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneTransporterInput(function).toString()
            CurrentlyGetsTransporterInteractingMedication(selector, termToFind, DrugInteraction.Type.INHIBITOR)
        }
    }

    private fun getsTransporterSubstrateMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneTransporterInput(function).toString()
            CurrentlyGetsTransporterInteractingMedication(selector, termToFind, DrugInteraction.Type.SUBSTRATE)
        }
    }

    private fun getsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val types = functionInputResolver().createManyStringsInput(function)
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

    companion object {
        fun extractCypString(cyp: Cyp): String {
            return cyp.toString().substring(3).replace("_", "/")
        }
    }
}