package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class MedicationRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    private val selector: MedicationSelector

    init {
        val interpreter: MedicationStatusInterpreter = MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date())
        selector = MedicationSelector(interpreter)
    }

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.CURRENTLY_GETS_NAME_X_MEDICATION to getsActiveMedicationWithConfiguredNameCreator(),
            EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION to getsActiveMedicationWithCategoryCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORY_X_MEDICATION_WITHIN_Y_WEEKS to hasRecentlyReceivedMedicationOfCategoryCreator(),
            EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION to getsQTProlongatingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_ANY_CYP to getsAnyCYPInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_CYP_X to getsCYPXInducingMedicationCreator(),
            EligibilityRule.HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS to hasRecentlyReceivedCYPXInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_CYP_X to getsCYPXInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_ANY_CYP to getsAnyCypInhibitingOrInducingMedication(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_CYP_X to getsCYPXInhibitingOrInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_CYP_X to getsCYPSubstrateMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_PGP to getsPGPInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_PGP to getsPGPSubstrateMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_BCRP to getsBCRPInhibitingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_SUBSTRATE_OF_BCRP to getsBCRPSubstrateMedicationCreator(),
            EligibilityRule.HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING to getsStableDosingAnticoagulantMedicationCreator(),
        )
    }

    private fun getsActiveMedicationWithConfiguredNameCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsMedicationOfName(selector, setOf(termToFind))
        }
    }

    private fun getsActiveMedicationWithCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val categoryInputs = functionInputResolver().createOneStringInput(function)
            val categories = determineCategories(categoryInputs)
            CurrentlyGetsMedicationOfCategory(selector, categories)
        }
    }

    private fun hasRecentlyReceivedMedicationOfCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val categoryInputs = functionInputResolver().createOneStringOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(categoryInputs.integer().toLong())
            val categories = determineCategories(categoryInputs.string())
            HasRecentlyReceivedMedicationOfCategory(selector, categories, maxStopDate)
        }
    }

    private fun getsQTProlongatingMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsQTProlongatingMedication(selector) }
    }

    private fun getsAnyCYPInducingMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsAnyCypInducingMedication(selector) }
    }

    private fun getsCYPXInducingMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsCypXInducingMedication(selector, termToFind)
        }
    }

    private fun hasRecentlyReceivedCYPXInducingMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.integer().toLong())
            HasRecentlyReceivedCypXInducingMedication(selector, input.string(), maxStopDate)
        }
    }

    private fun getsCYPXInhibitingMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsCypXInhibitingMedication(selector, termToFind)
        }
    }

    private fun getsAnyCypInhibitingOrInducingMedication(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsAnyCypInhibitingOrInducingMedication(selector) }
    }

    private fun getsCYPXInhibitingOrInducingMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsCypXInhibitingOrInducingMedication(selector, termToFind)
        }
    }

    private fun getsCYPSubstrateMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsCypXSubstrateMedication(selector, termToFind)
        }
    }

    private fun getsPGPInhibitingMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsPGPInhibitingMedication() }
    }

    private fun getsPGPSubstrateMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsPGPSubstrateMedication() }
    }

    private fun getsBCRPInhibitingMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsBCRPInhibitingMedication() }
    }

    private fun getsBCRPSubstrateMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsBCRPSubstrateMedication() }
    }

    private fun getsStableDosingAnticoagulantMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsStableMedicationOfCategory(selector, setOf("Antithrombotic agents", "Antihemorrhagics")) }
    }

    companion object {
        private val CATEGORIES_PER_MAIN_CATEGORY: Map<String, Set<String>> = mapOf(
            "Anticoagulants" to setOf("Antithrombotic agents", "Antihemorrhagics"),
            "Azole" to setOf("Imidazole and triazole derivatives", "Triazole and tetrazole derivatives", "Imidazole derivatives"),
            "Bone resorptive" to setOf("Calcium homeostasis", "Drugs affecting bone structure and mineralization"),
            "Coumarin derivative" to setOf("Vitamin K antagonists"),
            "Gonadorelin" to setOf(
                "Anti-gonadotropin-releasing hormones",
                "Gonadotropin-releasing hormones",
                "Antigonadotropins and similar agents",
                "Gonadotropin releasing hormone analogues"
            ),
        )

        private fun determineCategories(input: String): Map<String, Set<String>?> {
            return if (CATEGORIES_PER_MAIN_CATEGORY[input] != null) mapOf(input to CATEGORIES_PER_MAIN_CATEGORY[input]) else mapOf(
                input to setOf(
                    input
                )
            )
        }

        // Undetermined Cyp
        val UNDETERMINED_CYP = setOf("2J2")
    }
}