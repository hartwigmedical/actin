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
            EligibilityRule.CURRENTLY_GETS_CATEGORY_X_MEDICATION to getsActiveMedicationWithApproximateCategoryCreator(),
            EligibilityRule.HAS_RECEIVED_CATEGORY_X_MEDICATION_WITHIN_Y_WEEKS to hasRecentlyReceivedMedicationOfApproximateCategoryCreator(),
            EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION to getsAnticoagulantMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_AZOLE_MEDICATION to getsAzoleMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_BONE_RESORPTIVE_MEDICATION to getsBoneResorptiveMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_COUMARIN_DERIVATIVE_MEDICATION to getsCoumarinDerivativeMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_GONADORELIN_MEDICATION to getsGonadorelinMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION to getsImmunosuppressantMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_POTENTIALLY_QT_PROLONGATING_MEDICATION to getsQTProlongatingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_ANY_CYP to getsAnyCYPInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INDUCING_CYP_X to getsCYPXInducingMedicationCreator(),
            EligibilityRule.HAS_RECEIVED_MEDICATION_INDUCING_CYP_X_WITHIN_Y_WEEKS to hasRecentlyReceivedCYPXInducingMedicationCreator(),
            EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_CYP_X to getsCYPXInhibitingMedicationCreator(),
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

    private fun getsActiveMedicationWithApproximateCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val categoryTermToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsMedicationOfApproximateCategory(selector, categoryTermToFind)
        }
    }

    private fun hasRecentlyReceivedMedicationOfApproximateCategoryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringOneIntegerInput(function)
            val maxStopDate = referenceDateProvider().date().minusWeeks(input.integer().toLong())
            HasRecentlyReceivedMedicationOfApproximateCategory(selector, input.string(), maxStopDate)
        }
    }

    private fun getsAnticoagulantMedicationCreator(): FunctionCreator {
        return getsActiveMedicationWithExactCategoryCreator(ANTICOAGULANTS, VITAMIN_K_ANTAGONISTS)
    }

    private fun getsAzoleMedicationCreator(): FunctionCreator {
        return getsActiveMedicationWithExactCategoryCreator(TRIAZOLES, CUTANEOUS_IMIDAZOLES, OTHER_IMIDAZOLES)
    }

    private fun getsBoneResorptiveMedicationCreator(): FunctionCreator {
        return getsActiveMedicationWithExactCategoryCreator(BISPHOSPHONATES, CALCIUM_REGULATORY_MEDICATION)
    }

    private fun getsCoumarinDerivativeMedicationCreator(): FunctionCreator {
        return getsActiveMedicationWithExactCategoryCreator(VITAMIN_K_ANTAGONISTS)
    }

    private fun getsGonadorelinMedicationCreator(): FunctionCreator {
        return getsActiveMedicationWithExactCategoryCreator(GONADORELIN_ANTAGONISTS, GONADORELIN_AGONISTS)
    }

    private fun getsImmunosuppressantMedicationCreator(): FunctionCreator {
        return getsActiveMedicationWithExactCategoryCreator(SELECTIVE_IMMUNOSUPPRESSANTS, OTHER_IMMUNOSUPPRESSANTS)
    }

    private fun getsQTProlongatingMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsQTProlongatingMedication() }
    }

    private fun getsAnyCYPInducingMedicationCreator(): FunctionCreator {
        return FunctionCreator { CurrentlyGetsAnyCYPInducingMedication(selector) }
    }

    private fun getsCYPXInducingMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsCYPXInducingMedication(selector, termToFind)
        }
    }

    private fun hasRecentlyReceivedCYPXInducingMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringOneIntegerInput(function)
            HasRecentlyReceivedCYPXInducingMedication(input.string())
        }
    }

    private fun getsCYPXInhibitingMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsCYPXInhibitingMedication(selector, termToFind)
        }
    }

    private fun getsCYPXInhibitingOrInducingMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsCYPXInhibitingOrInducingMedication(selector, termToFind)
        }
    }

    private fun getsCYPSubstrateMedicationCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            CurrentlyGetsCYPXSubstrateMedication(selector, termToFind)
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
        return getsStableMedicationOfCategoryCreator(ANTICOAGULANTS)
    }

    private fun getsStableMedicationOfCategoryCreator(vararg categoriesToFind: String): FunctionCreator {
        return FunctionCreator { CurrentlyGetsStableMedicationOfCategory(selector, setOf(*categoriesToFind)) }
    }

    private fun getsActiveMedicationWithExactCategoryCreator(vararg categoriesToFind: String): FunctionCreator {
        return FunctionCreator { CurrentlyGetsMedicationOfExactCategory(selector, setOf(*categoriesToFind)) }
    }

    companion object {
        // Medication categories
        private const val ANTICOAGULANTS = "Anticoagulants"
        private const val VITAMIN_K_ANTAGONISTS = "Vitamin K Antagonists"
        private const val TRIAZOLES = "Triazoles"
        private const val CUTANEOUS_IMIDAZOLES = "Imidazoles, cutaneous"
        private const val OTHER_IMIDAZOLES = "Imidazoles, other"
        private const val BISPHOSPHONATES = "Bisphosphonates"
        private const val CALCIUM_REGULATORY_MEDICATION = "Calcium regulatory medication"
        private const val GONADORELIN_ANTAGONISTS = "Gonadorelin antagonists"
        private const val GONADORELIN_AGONISTS = "Gonadorelin agonists"
        private const val SELECTIVE_IMMUNOSUPPRESSANTS = "Immunosuppressants, selective"
        private const val OTHER_IMMUNOSUPPRESSANTS = "Immunosuppressants, other"
    }
}