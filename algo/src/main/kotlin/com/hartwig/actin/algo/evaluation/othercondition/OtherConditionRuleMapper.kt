package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class OtherConditionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_ICD_TITLE_X to hasPriorConditionWithConfiguredIcdTitleCreator(),
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME to hasPriorConditionWithConfiguredNameCreator(),
            EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.AUTOIMMUNE_DISEASE_DOID),
            EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE to hasHistoryOfCardiacDiseaseCreator(),
            EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.CARDIOVASCULAR_DISEASE_DOID),
            EligibilityRule.HAS_HISTORY_OF_CONGESTIVE_HEART_FAILURE_WITH_AT_LEAST_NYHA_CLASS_X to hasHistoryOfCongestiveHeartFailureWithNYHACreator(),
            EligibilityRule.HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.CENTRAL_NERVOUS_SYSTEM_DOID),
            EligibilityRule.HAS_HISTORY_OF_EYE_DISEASE to hasHistoryOfEyeDiseaseCreator(),
            EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.GASTROINTESTINAL_DISEASE_DOID),
            EligibilityRule.HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.IMMUNE_SYSTEM_DISEASE_DOID),
            EligibilityRule.HAS_HISTORY_OF_INTERSTITIAL_LUNG_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.INTERSTITIAL_LUNG_DISEASE_DOID),
            EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.LIVER_DISEASE_DOID),
            EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.LUNG_DISEASE_DOID),
            EligibilityRule.HAS_POTENTIAL_RESPIRATORY_COMPROMISE to hasPriorConditionWithIcdCodesFromListCreator(
                IcdConstants.RESPIRATORY_COMPROMISE_LIST, "potential respiratory compromise"
            ),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT to hasPriorConditionWithDoidCreator(DoidConstants.MYOCARDIAL_INFARCT_DOID),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS to hasRecentPriorConditionWithIcdCodeFromListCreator(
                listOf(IcdConstants.ACUTE_MYOCARDIAL_INFARCT_CODE), "myocardial infarct"
            ),
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_ICD_TITLE_X_WITHIN_Y_MONTHS to hasRecentPriorConditionWithConfiguredIcdCodeCreator(),
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME_WITHIN_Y_MONTHS to hasRecentPriorConditionWithConfiguredNameCreator(),
            EligibilityRule.HAS_HISTORY_OF_PNEUMONITIS to hasHistoryOfPneumonitisCreator(),
            EligibilityRule.HAS_HISTORY_OF_STROKE to hasHistoryOfStrokeCreator(),
            EligibilityRule.HAS_HISTORY_OF_STROKE_WITHIN_X_MONTHS to hasRecentPriorConditionWithIcdCodeFromListCreator(
                IcdConstants.STROKE_LIST,
                "cerebrovascular accident"
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT_WITHIN_X_MONTHS to hasRecentPriorConditionWithIcdCodeFromListCreator(
                IcdConstants.THROMBOEMBOLIC_EVENT_LIST,
                "thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT to hasPriorConditionWithIcdCodesFromListCreator(
                IcdConstants.THROMBOEMBOLIC_EVENT_LIST,
                "thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT to hasPriorConditionWithIcdCodesFromListCreator(
                IcdConstants.ARTERIAL_THROMBOEMBOLIC_EVENT_LIST, "Arterial thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT to hasPriorConditionWithIcdCodesFromListCreator(
                IcdConstants.VENOUS_THROMBOEMBOLIC_EVENT_LIST, "Venous thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.VASCULAR_DISEASE_DOID),
            EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION to hasSevereConcomitantIllnessCreator(),
            EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT to hasHadOrganTransplantCreator(),
            EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT_WITHIN_X_YEARS to hasHadOrganTransplantWithinYearsCreator(),
            EligibilityRule.HAS_GILBERT_DISEASE to hasPriorConditionWithDoidCreator(DoidConstants.GILBERT_DISEASE_DOID),
            EligibilityRule.HAS_HYPERTENSION to hasPriorConditionWithDoidCreator(DoidConstants.HYPERTENSION_DOID),
            EligibilityRule.HAS_HYPOTENSION to hasPriorConditionWithNameCreator(HYPOTENSION_NAME),
            EligibilityRule.HAS_DIABETES to hasPriorConditionWithDoidCreator(DoidConstants.DIABETES_DOID),
            EligibilityRule.HAS_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS to hasInheritedPredispositionToBleedingOrThrombosisCreator(),
            EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES to hasPotentialAbsorptionDifficultiesCreator(),
            EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES to hasOralMedicationDifficultiesCreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_CT to hasContraindicationToCTCreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_MRI to hasContraindicationToMRICreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI to hasContraindicationToMRICreator(),
            EligibilityRule.HAS_MRI_SCAN_DOCUMENTING_STABLE_DISEASE to hasMRIScanDocumentingStableDiseaseCreator(),
            EligibilityRule.IS_IN_DIALYSIS to isInDialysisCreator(),
            EligibilityRule.HAS_CHILD_PUGH_CLASS_X_LIVER_SCORE to hasChildPughClassCreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_FOR_STEREOTACTIC_RADIOSURGERY to hasPotentialContraIndicationForStereotacticRadiosurgeryCreator(),
            EligibilityRule.HAS_ADEQUATE_VENOUS_ACCESS to hasAdequateVenousAccesCreator(),
        )
    }

    private fun hasPriorConditionWithConfiguredIcdTitleCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val targetIcdTitle = functionInputResolver().createOneIcdTitleInput(function)
            HasHadPriorConditionWithIcd(icdModel(), targetIcdTitle)
        }
    }

    private fun hasPriorConditionWithConfiguredNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val nameToFind = functionInputResolver().createOneStringInput(function)
            HasHadPriorConditionWithName(nameToFind)
        }
    }

    private fun hasPriorConditionWithDoidCreator(targetIcdTitle: String): FunctionCreator {
        return { HasHadPriorConditionWithIcd(icdModel(), targetIcdTitle) }
    }

    private fun hasInheritedPredispositionToBleedingOrThrombosisCreator(): FunctionCreator {
        return { HasInheritedPredispositionToBleedingOrThrombosis(icdModel()) }
    }

    private fun hasRecentPriorConditionWithIcdCodeFromListCreator(
        targetIcdCodes: List<String>,
        diseaseDescription: String
    ): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxMonthsAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong())
            HasHadPriorConditionWithIcdCodeFromListRecently(icdModel(), targetIcdCodes, diseaseDescription, minDate)
        }
    }

    private fun hasRecentPriorConditionWithConfiguredIcdCodeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneIcdTitleOneIntegerInput(function)
            val targetIcdCode = icdModel().titleToCodeMap[input.icdTitle]
            val maxMonthsAgo = input.integer
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong())
            HasHadPriorConditionWithIcdCodeFromListRecently(
                icdModel(), listOf(targetIcdCode!!), input.icdTitle, minDate
            )
        }
    }

    private fun hasRecentPriorConditionWithConfiguredNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneStringOneIntegerInput(function)
            val nameToFind = input.string
            val maxMonthsAgo = input.integer
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong())
            HasHadPriorConditionWithNameRecently(nameToFind, minDate)
        }
    }

    private fun hasPriorConditionWithIcdCodesFromListCreator(targetIcdCodes: List<String>, priorOtherConditionTerm: String): FunctionCreator {
        return { HasHadPriorConditionWithIcdCodeFromList(icdModel(), targetIcdCodes, priorOtherConditionTerm)
        }
    }

    private fun hasPriorConditionWithNameCreator(nameToFind: String): FunctionCreator {
        return { HasHadPriorConditionWithName(nameToFind) }
    }

    private fun hasHistoryOfCardiacDiseaseCreator(): FunctionCreator {
        return {
            HasHadPriorConditionComplicationOrToxicityWithIcdCode(
                icdModel(),
                IcdConstants.HEART_DISEASE_LIST,
                "cardiac disease",
                referenceDateProvider().date()
            )
        }
    }

    private fun hasHistoryOfCongestiveHeartFailureWithNYHACreator(): FunctionCreator {
        return { HasHistoryOfCongestiveHeartFailureWithNYHA() }
    }

    private fun hasHistoryOfEyeDiseaseCreator(): FunctionCreator {
        return {
            HasHadPriorConditionComplicationOrToxicityWithIcdCode(
                icdModel(),
                listOf(IcdConstants.EYE_DISEASE_CHAPTER),
                "eye disease",
                referenceDateProvider().date()
            )
        }
    }

    private fun hasHistoryOfPneumonitisCreator(): FunctionCreator {
        return { HasHistoryOfPneumonitis(doidModel()) }
    }

    private fun hasHistoryOfStrokeCreator(): FunctionCreator {
        return {
            HasHadPriorConditionComplicationOrToxicityWithIcdCode(
                icdModel(),
                IcdConstants.STROKE_LIST,
                "cerebrovascular accident",
                referenceDateProvider().date()
            )
        }
    }

    private fun hasSevereConcomitantIllnessCreator(): FunctionCreator {
        return { HasSevereConcomitantIllness() }
    }

    private fun hasHadOrganTransplantCreator(): FunctionCreator {
        return { HasHadOrganTransplant(null) }
    }

    private fun hasHadOrganTransplantWithinYearsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxYearsAgo = functionInputResolver().createOneIntegerInput(function)
            val minYear = referenceDateProvider().year() - maxYearsAgo
            HasHadOrganTransplant(minYear)
        }
    }

    private fun hasPotentialAbsorptionDifficultiesCreator(): FunctionCreator {
        return { HasPotentialAbsorptionDifficulties(doidModel()) }
    }

    private fun hasOralMedicationDifficultiesCreator(): FunctionCreator {
        return { HasOralMedicationDifficulties() }
    }

    private fun hasContraindicationToCTCreator(): FunctionCreator {
        return { HasContraindicationToCT(doidModel()) }
    }

    private fun hasContraindicationToMRICreator(): FunctionCreator {
        return { HasContraindicationToMRI(doidModel()) }
    }

    private fun hasMRIScanDocumentingStableDiseaseCreator(): FunctionCreator {
        return { HasMRIScanDocumentingStableDisease() }
    }

    private fun isInDialysisCreator(): FunctionCreator {
        return { IsInDialysis() }
    }

    private fun hasChildPughClassCreator(): FunctionCreator {
        return { HasChildPughClass(doidModel()) }
    }

    private fun hasPotentialContraIndicationForStereotacticRadiosurgeryCreator(): FunctionCreator {
        return { HasPotentialContraIndicationForStereotacticRadiosurgery() }
    }

    private fun hasAdequateVenousAccesCreator(): FunctionCreator {
        return { HasAdequateVenousAccess() }
    }

    companion object {
        private const val HYPOTENSION_NAME: String = "hypotension"
    }
}