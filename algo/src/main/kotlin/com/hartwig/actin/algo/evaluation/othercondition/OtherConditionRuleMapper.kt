package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class OtherConditionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_ICD_TITLE_X to hasPriorConditionWithConfiguredIcdTitleCreator(),
            EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                IcdConstants.AUTOIMMUNE_DISEASE_SET.map { IcdCode(it) }.toSet(),
                "autoimmune disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE to hasHistoryOfCardiacDiseaseCreator(),
            EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.CIRCULATORY_SYSTEM_DISEASE_CHAPTER)),
                "cardiovascular disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_CONGESTIVE_HEART_FAILURE_WITH_AT_LEAST_NYHA_CLASS_X to hasHistoryOfCongestiveHeartFailureWithNYHACreator(),
            EligibilityRule.HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(
                    IcdCode(
                        IcdConstants.NERVOUS_SYSTEM_DISEASE_CHAPTER
                    )
                ), "CNS disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_EYE_DISEASE to hasHistoryOfEyeDiseaseCreator(),
            EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(
                    IcdCode(
                        IcdConstants.DIGESTIVE_SYSTEM_DISEASE_CHAPTER
                    )
                ), "gastrointestinal disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_IMMUNE_SYSTEM_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.IMMUNE_SYSTEM_DISEASE_CHAPTER)),
                "immune system disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_INTERSTITIAL_LUNG_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(
                    IcdCode(
                        IcdConstants.LUNG_INTERSTITIAL_DISEASES_BLOCK
                    )
                ), "interstitial lung disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.LIVER_DISEASE_BLOCK)),
                "liver disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(IcdConstants.RESPIRATORY_COMPROMISE_SET.map {
                IcdCode(
                    it
                )
            }.toSet(), "lung disease"),
            EligibilityRule.HAS_POTENTIAL_RESPIRATORY_COMPROMISE to hasPriorConditionWithIcdCodesFromSetCreator(
                IcdConstants.RESPIRATORY_COMPROMISE_SET.map { IcdCode(it) }.toSet(), "potential respiratory compromise"
            ),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.ACUTE_MYOCARDIAL_INFARCT_CODE)),
                "myocardial infarct"
            ),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS to hasRecentPriorConditionWithIcdCodeFromSetCreator(
                setOf(IcdCode(IcdConstants.ACUTE_MYOCARDIAL_INFARCT_CODE)), "myocardial infarct"
            ),
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_ICD_TITLE_X_WITHIN_Y_MONTHS to hasRecentPriorConditionWithConfiguredIcdCodeCreator(),
            EligibilityRule.HAS_HISTORY_OF_PNEUMONITIS to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.PNEUMONITIS_BLOCK)),
                "pneumonitis"
            ),
            EligibilityRule.HAS_HISTORY_OF_STROKE to hasHistoryOfStrokeCreator(),
            EligibilityRule.HAS_HISTORY_OF_STROKE_WITHIN_X_MONTHS to hasRecentPriorConditionWithIcdCodeFromSetCreator(
                IcdConstants.STROKE_SET.map { IcdCode(it) }.toSet(),
                "CVA"
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT_WITHIN_X_MONTHS to hasRecentPriorConditionWithIcdCodeFromSetCreator(
                IcdConstants.THROMBOEMBOLIC_EVENT_SET.map { IcdCode(it) }.toSet(),
                "thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT to hasPriorConditionWithIcdCodesFromSetCreator(
                IcdConstants.THROMBOEMBOLIC_EVENT_SET.map { IcdCode(it) }.toSet(),
                "thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT to hasPriorConditionWithIcdCodesFromSetCreator(
                IcdConstants.ARTERIAL_THROMBOEMBOLIC_EVENT_SET.map { IcdCode(it) }.toSet(), "Arterial thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT to hasPriorConditionWithIcdCodesFromSetCreator(
                IcdConstants.VENOUS_THROMBOEMBOLIC_EVENT_SET.map { IcdCode(it) }.toSet(), "Venous thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(
                    IcdCode(IcdConstants.ARTERY_DISEASE_BLOCK),
                    IcdCode(IcdConstants.VEIN_DISEASE_BLOCK)
                ), "vascular disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_ULCER to {
                HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                    icdModel(), IcdConstants.ULCER_SET.map { IcdCode(it) }.toSet(), "ulcer", referenceDateProvider().date()
                )
            },
            EligibilityRule.HAS_HISTORY_OF_BLEEDING to {
                HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                    icdModel(), IcdConstants.BLEEDING_SET.map { IcdCode(it) }.toSet(), "bleeding", referenceDateProvider().date()
                )
            },
            EligibilityRule.HAS_HISTORY_OF_WOUND to {
                HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                    icdModel(), IcdConstants.WOUND_SET.map { IcdCode(it) }.toSet(), "wound", referenceDateProvider().date()
                )
            },
            EligibilityRule.HAS_HISTORY_OF_BONE_FRACTURE to {
                HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                    icdModel(), IcdConstants.BONE_FRACTURE_SET.map { IcdCode(it) }.toSet(), "bone fracture", referenceDateProvider().date()
                )
            },
            EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION to hasSevereConcomitantIllnessCreator(),
            EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT to hasHadOrganTransplantCreator(),
            EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT_WITHIN_X_YEARS to hasHadOrganTransplantWithinYearsCreator(),
            EligibilityRule.HAS_GILBERT_DISEASE to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.GILBERT_SYNDROME_CODE)),
                "Gilbert disease"
            ),
            EligibilityRule.HAS_HYPERTENSION to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.HYPERTENSIVE_DISEASES_BLOCK)),
                "hypertension"
            ),
            EligibilityRule.HAS_HYPOTENSION to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.HYPOTENSION_BLOCK)),
                "hypotension"
            ),
            EligibilityRule.HAS_DIABETES to hasPriorConditionWithIcdCodesFromSetCreator(
                setOf(IcdCode(IcdConstants.DIABETES_MELLITUS_BLOCK)),
                "diabetes"
            ),
            EligibilityRule.HAS_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS to hasInheritedPredispositionToBleedingOrThrombosisCreator(),
            EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES to hasPotentialAbsorptionDifficultiesCreator(),
            EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES to {
                HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                    icdModel(),
                    listOf(IcdConstants.FUNCTIONAL_SWALLOWING_DISORDER_CODE, IcdConstants.DISORDERS_OF_ORAL_MUCOSA_CODE).map { IcdCode(it) }.toSet(),
                    "potential oral medication difficulties",
                    referenceDateProvider().date()
                )
            },
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_CT to hasContraindicationToCTCreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_MRI to hasContraindicationToMRICreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI to hasContraindicationToMRICreator(),
            EligibilityRule.HAS_MRI_SCAN_DOCUMENTING_STABLE_DISEASE to hasMRIScanDocumentingStableDiseaseCreator(),
            EligibilityRule.IS_IN_DIALYSIS to isInDialysisCreator(),
            EligibilityRule.HAS_CHILD_PUGH_SCORE_X to hasChildPughScoreCreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_FOR_STEREOTACTIC_RADIOSURGERY to hasPotentialContraIndicationForStereotacticRadiosurgeryCreator(),
            EligibilityRule.HAS_ADEQUATE_VENOUS_ACCESS to hasAdequateVenousAccesCreator(),
            EligibilityRule.MEETS_REQUIREMENTS_DURING_SIX_MINUTE_WALKING_TEST to { MeetsSixMinuteWalkingTestRequirements() }
        )
    }

    private fun hasPriorConditionWithConfiguredIcdTitleCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val targetIcdTitle = functionInputResolver().createOneIcdTitleInput(function)
            val icdCode = icdModel().resolveCodeForTitle(targetIcdTitle)!!
            HasHadOtherConditionWithIcdCodeFromSet(icdModel(), setOf(icdCode), targetIcdTitle)
        }
    }

    private fun hasInheritedPredispositionToBleedingOrThrombosisCreator(): FunctionCreator {
        return { HasInheritedPredispositionToBleedingOrThrombosis(icdModel()) }
    }

    private fun hasRecentPriorConditionWithIcdCodeFromSetCreator(
        targetIcdCodes: Set<IcdCode>,
        diseaseDescription: String
    ): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxMonthsAgo = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong())
            HasHadOtherConditionWithIcdCodeFromSetRecently(icdModel(), targetIcdCodes, diseaseDescription, minDate)
        }
    }

    private fun hasRecentPriorConditionWithConfiguredIcdCodeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneIcdTitleOneIntegerInput(function)
            val targetIcdCode = icdModel().resolveCodeForTitle(input.icdTitle)!!
            val maxMonthsAgo = input.integer
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong())
            HasHadOtherConditionWithIcdCodeFromSetRecently(
                icdModel(), setOf(targetIcdCode), input.icdTitle, minDate
            )
        }
    }

    private fun hasPriorConditionWithIcdCodesFromSetCreator(
        targetIcdCodes: Set<IcdCode>,
        otherConditionTerm: String
    ): FunctionCreator {
        return {
            HasHadOtherConditionWithIcdCodeFromSet(icdModel(), targetIcdCodes, otherConditionTerm)
        }
    }

    private fun hasHistoryOfCardiacDiseaseCreator(): FunctionCreator {
        return {
            HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                icdModel(),
                IcdConstants.HEART_DISEASE_SET.map { IcdCode(it) }.toSet(),
                "cardiac disease",
                referenceDateProvider().date()
            )
        }
    }

    private fun hasHistoryOfCongestiveHeartFailureWithNYHACreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val input = functionInputResolver().createOneNyhaClassInput(function)
            HasHistoryOfCongestiveHeartFailureWithNYHA(input, icdModel())
        }
    }

    private fun hasHistoryOfEyeDiseaseCreator(): FunctionCreator {
        return {
            HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                icdModel(),
                setOf(IcdConstants.EYE_DISEASE_CHAPTER).map { IcdCode(it) }.toSet(),
                "eye disease",
                referenceDateProvider().date()
            )
        }
    }

    private fun hasHistoryOfStrokeCreator(): FunctionCreator {
        return {
            HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                icdModel(),
                IcdConstants.STROKE_SET.map { IcdCode(it) }.toSet(),
                "CVA",
                referenceDateProvider().date()
            )
        }
    }

    private fun hasSevereConcomitantIllnessCreator(): FunctionCreator {
        return { HasSevereConcomitantIllness() }
    }

    private fun hasHadOrganTransplantCreator(): FunctionCreator {
        return { HasHadOrganTransplant(icdModel(), null) }
    }

    private fun hasHadOrganTransplantWithinYearsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxYearsAgo = functionInputResolver().createOneIntegerInput(function)
            val minYear = referenceDateProvider().year() - maxYearsAgo
            HasHadOrganTransplant(icdModel(), minYear)
        }
    }

    private fun hasPotentialAbsorptionDifficultiesCreator(): FunctionCreator {
        return { HasPotentialAbsorptionDifficulties(icdModel()) }
    }

    private fun hasContraindicationToCTCreator(): FunctionCreator {
        return { HasContraindicationToCT(icdModel()) }
    }

    private fun hasContraindicationToMRICreator(): FunctionCreator {
        return { HasContraindicationToMRI(icdModel()) }
    }

    private fun hasMRIScanDocumentingStableDiseaseCreator(): FunctionCreator {
        return { HasMRIScanDocumentingStableDisease() }
    }

    private fun isInDialysisCreator(): FunctionCreator {
        return { IsInDialysis(icdModel()) }
    }

    private fun hasChildPughScoreCreator(): FunctionCreator {
        return { HasChildPughScore(icdModel()) }
    }

    private fun hasPotentialContraIndicationForStereotacticRadiosurgeryCreator(): FunctionCreator {
        return { HasPotentialContraIndicationForStereotacticRadiosurgery() }
    }

    private fun hasAdequateVenousAccesCreator(): FunctionCreator {
        return { HasAdequateVenousAccess() }
    }
}