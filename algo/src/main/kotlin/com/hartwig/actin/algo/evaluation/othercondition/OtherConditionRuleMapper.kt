package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class OtherConditionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_TERM_X to hasPriorConditionWithConfiguredDOIDTermCreator(),
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
            EligibilityRule.HAS_POTENTIAL_RESPIRATORY_COMPROMISE to hasPriorConditionWithDoidsFromSetCreator(
                DoidConstants.RESPIRATORY_COMPROMISE_DOID_SET, "Potential respiratory compromise"
            ),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT to hasPriorConditionWithDoidCreator(DoidConstants.MYOCARDIAL_INFARCT_DOID),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS to hasRecentPriorConditionWithDoidCreator(DoidConstants.MYOCARDIAL_INFARCT_DOID),
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_DOID_TERM_X_WITHIN_Y_MONTHS to hasRecentPriorConditionWithConfiguredDoidTermCreator(),
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_X_BY_NAME_WITHIN_Y_MONTHS to hasRecentPriorConditionWithConfiguredNameCreator(),
            EligibilityRule.HAS_HISTORY_OF_PNEUMONITIS to hasHistoryOfPneumonitisCreator(),
            EligibilityRule.HAS_HISTORY_OF_STROKE to hasHistoryOfStrokeCreator(),
            EligibilityRule.HAS_HISTORY_OF_STROKE_WITHIN_X_MONTHS to hasRecentPriorConditionWithDoidCreator(DoidConstants.STROKE_DOID),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT_WITHIN_X_MONTHS to hasRecentPriorConditionWithDoidsFromSetCreator(
                DoidConstants.THROMBOEMBOLIC_EVENT_DOID_SET,
                "Thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT to hasPriorConditionWithDoidsFromSetCreator(
                DoidConstants.THROMBOEMBOLIC_EVENT_DOID_SET,
                "Thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT to hasPriorConditionWithDoidsFromSetCreator(
                setOf(DoidConstants.MYOCARDIAL_INFARCT_DOID, DoidConstants.STROKE_DOID), "Arterial thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT to hasPriorConditionWithDoidsFromSetCreator(
                setOf(DoidConstants.THROMBOSIS_DOID, DoidConstants.PULMONARY_EMBOLISM_DOID), "Venous thrombo-embolic event"
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

    private fun hasPriorConditionWithConfiguredDOIDTermCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val doidTermToFind = functionInputResolver().createOneDoidTermInput(function)
            HasHadPriorConditionWithDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToFind)!!)
        }
    }

    private fun hasPriorConditionWithConfiguredNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val nameToFind = functionInputResolver().createOneStringInput(function)
            HasHadPriorConditionWithName(nameToFind)
        }
    }

    private fun hasPriorConditionWithDoidCreator(doidToFind: String): FunctionCreator {
        return { HasHadPriorConditionWithDoid(doidModel(), doidToFind) }
    }

    private fun hasInheritedPredispositionToBleedingOrThrombosisCreator(): FunctionCreator {
        return { HasInheritedPredispositionToBleedingOrThrombosis(doidModel()) }
    }

    private fun hasRecentPriorConditionWithDoidCreator(doidToFind: String): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxMonthsAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong())
            HasHadPriorConditionWithDoidsFromSetRecently(
                doidModel(), setOf(doidToFind), doidModel().resolveTermForDoid(doidToFind) ?: "DOID $doidToFind", minDate
            )
        }
    }

    private fun hasRecentPriorConditionWithConfiguredDoidTermCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneDoidTermOneIntegerInput(function)
            val doidTermToFind = input.doidTerm
            val maxMonthsAgo = input.integer
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong())
            HasHadPriorConditionWithDoidsFromSetRecently(
                doidModel(), setOf(doidModel().resolveDoidForTerm(doidTermToFind)!!), doidTermToFind, minDate
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

    private fun hasPriorConditionWithDoidsFromSetCreator(doidsToFind: Set<String>, priorOtherConditionTerm: String): FunctionCreator {
        return { function: EligibilityFunction ->
            HasHadPriorConditionWithDoidsFromSet(doidModel(), doidsToFind, priorOtherConditionTerm)
        }
    }

    private fun hasRecentPriorConditionWithDoidsFromSetCreator(doidsToFind: Set<String>, priorOtherConditionTerm: String): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxMonthsAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong())
            HasHadPriorConditionWithDoidsFromSetRecently(doidModel(), doidsToFind, priorOtherConditionTerm, minDate)
        }
    }

    private fun hasPriorConditionWithNameCreator(nameToFind: String): FunctionCreator {
        return { HasHadPriorConditionWithName(nameToFind) }
    }

    private fun hasHistoryOfCardiacDiseaseCreator(): FunctionCreator {
        return {
            Or(
                listOf(
                    HasHadPriorConditionWithDoidComplicationOrToxicity(
                    doidModel(),
                    DoidConstants.HEART_DISEASE_DOID,
                    CARDIAC_DISEASE_COMPLICATION_AND_TOXICITY_CATEGORY,
                    CARDIAC_DISEASE_COMPLICATION_AND_TOXICITY_CATEGORY
                    ),
                    HasHadPriorConditionWithDoid(doidModel(), DoidConstants.CORONARY_ARTERY_DISEASE_DOID)
                )
            )
        }
    }

    private fun hasHistoryOfCongestiveHeartFailureWithNYHACreator(): FunctionCreator {
        return { HasHistoryOfCongestiveHeartFailureWithNYHA() }
    }

    private fun hasHistoryOfEyeDiseaseCreator(): FunctionCreator {
        return {
            HasHadPriorConditionWithDoidComplicationOrToxicity(
                doidModel(),
                DoidConstants.EYE_DISEASE_DOID,
                EYE_DISEASE_COMPLICATION_AND_TOXICITY_CATEGORY,
                EYE_DISEASE_COMPLICATION_AND_TOXICITY_CATEGORY
            )
        }
    }

    private fun hasHistoryOfPneumonitisCreator(): FunctionCreator {
        return { HasHistoryOfPneumonitis(doidModel()) }
    }

    private fun hasHistoryOfStrokeCreator(): FunctionCreator {
        return {
            HasHadPriorConditionWithDoidComplicationOrToxicity(
                doidModel(),
                DoidConstants.STROKE_DOID,
                CEREBROVASCULAR_ACCIDENT_COMPLICATION_AND_TOXICITY_CATEGORY,
                CEREBROVASCULAR_ACCIDENT_COMPLICATION_AND_TOXICITY_CATEGORY
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
        private const val CARDIAC_DISEASE_COMPLICATION_AND_TOXICITY_CATEGORY: String = "cardiac disease"
        private const val EYE_DISEASE_COMPLICATION_AND_TOXICITY_CATEGORY: String = "eye disease"
        private const val CEREBROVASCULAR_ACCIDENT_COMPLICATION_AND_TOXICITY_CATEGORY: String = "cerebrovascular accident"
    }
}