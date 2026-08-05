package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.IcdTitleParameter
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.ManyIcdTitlesParameter
import com.hartwig.actin.datamodel.trial.ManyStringsParameter
import com.hartwig.actin.datamodel.trial.NyhaClassParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.trial.input.EligibilityRule
import com.hartwig.actin.trial.input.datamodel.NyhaClass

class ComorbidityRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.AUTOIMMUNE_DISEASE_SET,
                evaluationLabels.comorbidity.descriptionAutoimmuneDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.HEART_DISEASE_SET,
                evaluationLabels.comorbidity.descriptionCardiacDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                IcdConstants.HEART_DISEASE_SET.map { IcdCode(it) }.toSet(), evaluationLabels.comorbidity.descriptionCardiacDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.CIRCULATORY_SYSTEM_DISEASE_CHAPTER),
                evaluationLabels.comorbidity.descriptionCardiovascularDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_CONGESTIVE_HEART_FAILURE_WITH_AT_LEAST_NYHA_CLASS_X to hasHistoryOfCongestiveHeartFailureWithNYHACreator(),
            EligibilityRule.HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.NERVOUS_SYSTEM_DISEASE_CHAPTER),
                evaluationLabels.comorbidity.descriptionCnsDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_EYE_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.EYE_DISEASE_CHAPTER),
                evaluationLabels.comorbidity.descriptionEyeDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.DIGESTIVE_SYSTEM_DISEASE_CHAPTER),
                evaluationLabels.comorbidity.descriptionGastrointestinalDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_FISTULA to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.FISTULA_OF_OESOPHAGUS,
                    IcdConstants.GASTRIC_FISTULA,
                    IcdConstants.FISTULA_OF_SMALL_INTESTINE,
                    IcdConstants.FISTULA_OF_LARGE_INTESTINE,
                    IcdConstants.FISTULA_OF_APPENDIX,
                ), evaluationLabels.comorbidity.descriptionGastrointestinalFistula()
            ),
            EligibilityRule.HAS_HISTORY_OF_IMMUNODEFICIENCY to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.IMMUNO_DEFICIENCY_SET,
                evaluationLabels.comorbidity.descriptionImmunodeficiency()
            ),
            EligibilityRule.HAS_HISTORY_OF_INTERSTITIAL_LUNG_DISEASE_INCLUDING_PNEUMONITIS to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.INTERSTITIAL_LUNG_DISEASE_SET + IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK,
                evaluationLabels.comorbidity.descriptionInterstitialLungDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.LIVER_DISEASE_BLOCK),
                evaluationLabels.comorbidity.descriptionLiverDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.RESPIRATORY_COMPROMISE_SET,
                evaluationLabels.comorbidity.descriptionLungDisease()
            ),
            EligibilityRule.HAS_POTENTIAL_RESPIRATORY_COMPROMISE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.RESPIRATORY_COMPROMISE_SET, evaluationLabels.comorbidity.descriptionPotentialRespiratoryCompromise()
            ),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.ACUTE_MYOCARDIAL_INFARCT_CODE),
                evaluationLabels.comorbidity.descriptionMyocardialInfarct()
            ),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                setOf(IcdCode(IcdConstants.ACUTE_MYOCARDIAL_INFARCT_CODE)), evaluationLabels.comorbidity.descriptionMyocardialInfarct()
            ),
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_ICD_TITLE_X_WITHIN_Y_MONTHS to hasHadOtherConditionMatchingSpecificIcdTitleRecentlyCreator(),
            EligibilityRule.HAS_HISTORY_OF_PNEUMONITIS to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK,
                    IcdConstants.IDIOPATHIC_INTERSTITIAL_PNEUMONITIS_CODE,
                    IcdConstants.IDIOPATHIC_EOSINOPHILIC_PNEUMONITIS_CODE,
                    IcdConstants.ACUTE_RADIATION_PNEUMONITIS_CODE
                ),
                evaluationLabels.comorbidity.descriptionPneumonitis()
            ),
            EligibilityRule.HAS_HISTORY_OF_STROKE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.STROKE_SET,
                evaluationLabels.comorbidity.descriptionCva()
            ),
            EligibilityRule.HAS_HISTORY_OF_STROKE_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                IcdConstants.STROKE_SET.map { IcdCode(it) }.toSet(),
                evaluationLabels.comorbidity.descriptionCva()
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                IcdConstants.THROMBOEMBOLIC_EVENT_SET.map { IcdCode(it) }.toSet(),
                evaluationLabels.comorbidity.descriptionThromboembolicEvent()
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.THROMBOEMBOLIC_EVENT_SET,
                evaluationLabels.comorbidity.descriptionThromboembolicEvent()
            ),
            EligibilityRule.HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.ARTERIAL_THROMBOEMBOLIC_EVENT_SET,
                evaluationLabels.comorbidity.descriptionArterialThromboembolicEvent()
            ),
            EligibilityRule.HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                IcdConstants.ARTERIAL_THROMBOEMBOLIC_EVENT_SET.map { IcdCode(it) }.toSet(),
                evaluationLabels.comorbidity.descriptionArterialThromboembolicEvent()
            ),
            EligibilityRule.HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.VENOUS_THROMBOEMBOLIC_EVENT_SET, evaluationLabels.comorbidity.descriptionVenousThromboembolicEvent()
            ),
            EligibilityRule.HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                IcdConstants.VENOUS_THROMBOEMBOLIC_EVENT_SET.map { IcdCode(it) }.toSet(),
                evaluationLabels.comorbidity.descriptionVenousThromboembolicEvent()
            ),
            EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.ARTERY_DISEASE_BLOCK,
                    IcdConstants.VEIN_DISEASE_BLOCK
                ), evaluationLabels.comorbidity.descriptionVascularDisease()
            ),
            EligibilityRule.HAS_HISTORY_OF_ULCER to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.ULCER_SET,
                evaluationLabels.comorbidity.descriptionUlcer()
            ),
            EligibilityRule.HAS_HISTORY_OF_BLEEDING to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.BLEEDING_SET,
                evaluationLabels.comorbidity.descriptionBleeding()
            ),
            EligibilityRule.HAS_HISTORY_OF_WOUND to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.WOUND_SET,
                evaluationLabels.comorbidity.descriptionWound()
            ),
            EligibilityRule.HAS_HISTORY_OF_BONE_FRACTURE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.BONE_FRACTURE_SET,
                evaluationLabels.comorbidity.descriptionBoneFracture()
            ),
            EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION to hasSevereConcomitantIllnessCreator(),
            EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT to hasHadOrganTransplantCreator(),
            EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT_WITHIN_X_YEARS to hasHadOrganTransplantWithinYearsCreator(),
            EligibilityRule.HAS_GILBERT_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.GILBERT_SYNDROME_CODE),
                evaluationLabels.comorbidity.descriptionGilbertDisease()
            ),
            EligibilityRule.HAS_HYPERTENSION to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.HYPERTENSIVE_DISEASES_BLOCK),
                evaluationLabels.comorbidity.descriptionHypertension()
            ),
            EligibilityRule.HAS_HYPOTENSION to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.HYPOTENSION_BLOCK),
                evaluationLabels.comorbidity.descriptionHypotension()
            ),
            EligibilityRule.HAS_DIABETES to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.DIABETES_MELLITUS_BLOCK),
                evaluationLabels.comorbidity.descriptionDiabetes()
            ),
            EligibilityRule.HAS_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS to hasInheritedPredispositionToBleedingOrThrombosisCreator(),
            EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES to hasPotentialAbsorptionDifficultiesCreator(),
            EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES to hasHadComorbiditiesWithIcdCodeCreator(
                listOf(
                    IcdConstants.FUNCTIONAL_SWALLOWING_DISORDER_CODE,
                    IcdConstants.DISORDERS_OF_ORAL_MUCOSA_CODE
                ),
                evaluationLabels.comorbidity.descriptionPotentialOralMedicationDifficulties(),
            ),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_CT to hasContraindicationToCTCreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_MRI to hasContraindicationToMRICreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_TO_PET_MRI to hasContraindicationToMRICreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_FOR_PET_CT_SCAN to hasContraindicationToCTCreator(),
            EligibilityRule.HAS_MRI_SCAN_DOCUMENTING_STABLE_DISEASE to hasMRIScanDocumentingStableDiseaseCreator(),
            EligibilityRule.IS_IN_DIALYSIS to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.DIALYSIS_CARE_CODE,
                    IcdConstants.DEPENDENCE_ON_RENAL_DIALYSIS_CODE
                ), evaluationLabels.comorbidity.descriptionRenalDialysis()
            ),
            EligibilityRule.HAS_CHILD_PUGH_SCORES_X to hasChildPughScoreCreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_FOR_STEREOTACTIC_RADIOSURGERY to
                    hasPotentialContraIndicationForStereotacticRadiosurgeryCreator(),
            EligibilityRule.HAS_ADEQUATE_VENOUS_ACCESS to hasAdequateVenousAccessCreator(),
            EligibilityRule.MEETS_REQUIREMENTS_DURING_SIX_MINUTE_WALKING_TEST to {
                MeetsSixMinuteWalkingTestRequirements(evaluationLabels.comorbidity)
            },
            EligibilityRule.HAS_COMORBIDITY_WITH_ANY_ICD_TITLE_X to hasHadComorbiditiesWithIcdCodeCreator(),
            EligibilityRule.HAS_POTENTIAL_UNCONTROLLED_TUMOR_RELATED_PAIN to hasPotentialUncontrolledTumorRelatedPainCreator(),
            EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE to hasLeptomeningealDiseaseCreator(),
            EligibilityRule.HAS_SPINAL_CORD_COMPRESSION to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.MYELOPATHY,
                    IcdConstants.TRAUMATIC_SPINAL_CORD_COMPRESSION,
                    IcdConstants.OTHER_SPECIFIED_SPINAL_CORD_DISORDERS_EXCLUDING_TRAUMA
                ),
                evaluationLabels.comorbidity.descriptionSpinalCordCompression()
            ),
            EligibilityRule.HAS_PLEURAL_EFFUSION to
                    hasHadComorbiditiesWithIcdCodeCreator(
                        setOf(
                            IcdConstants.PLEURAL_EFFUSION_CODE,
                            IcdConstants.MALIGNANT_NEOPLASM_METASTASIS_IN_PLEURA_CODE,
                            IcdConstants.PLEURISY_CODE
                        ),
                        evaluationLabels.comorbidity.descriptionPleuralEffusion()
                    ),
            EligibilityRule.HAS_PERITONEAL_EFFUSION to
                    hasHadComorbiditiesWithIcdCodeCreator(
                        setOf(
                            IcdConstants.MALIGNANT_NEORPLASM_METASTASIS_IN_RETROPERITONEUM_OR_PERITONEUM_BLOCK,
                            IcdConstants.ASCITES_CODE
                        ),
                        evaluationLabels.comorbidity.descriptionPeritonealEffusion()
                    ),
            EligibilityRule.HAS_POTENTIAL_DISRUPTION_OF_LYMPHATIC_DRAINAGE to hasPotentialDisruptionOfLymphaticDrainageCreator()
        )
    }

    private fun hasInheritedPredispositionToBleedingOrThrombosisCreator(): FunctionCreator {
        return { HasInheritedPredispositionToBleedingOrThrombosis(icdModel, evaluationLabels.comorbidity) }
    }

    private fun hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
        targetIcdCodes: Set<IcdCode>,
        diseaseDescription: String
    ): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxMonthsAgo = function.param<IntegerParameter>(0).value
            val minDate = referenceDateProvider.date().minusMonths(maxMonthsAgo.toLong() - 1)
            HasHadOtherConditionWithIcdCodeFromSetRecently(
                icdModel,
                targetIcdCodes,
                diseaseDescription,
                minDate,
                maxMonthsAgo,
                evaluationLabels.comorbidity
            )
        }
    }

    private fun hasHadOtherConditionMatchingSpecificIcdTitleRecentlyCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.ICD_TITLE, Parameter.Type.INTEGER)
            val icdTitle = toIcdTitle(function.param<IcdTitleParameter>(0).value)
            val maxMonthsAgo = function.param<IntegerParameter>(1).value
            val targetIcdCode = icdModel.resolveCodeForTitle(icdTitle)!!
            val minDate = referenceDateProvider.date().minusMonths(maxMonthsAgo.toLong() - 1)
            HasHadOtherConditionWithIcdCodeFromSetRecently(
                icdModel,
                setOf(targetIcdCode),
                icdTitle,
                minDate,
                maxMonthsAgo,
                evaluationLabels.comorbidity
            )
        }
    }

    private fun hasHistoryOfCongestiveHeartFailureWithNYHACreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val nyhaClass = NyhaClass.valueOf(function.param<NyhaClassParameter>(0).value)
            HasHistoryOfCongestiveHeartFailureWithNYHA(nyhaClass, icdModel, evaluationLabels.comorbidity)
        }
    }

    private fun hasSevereConcomitantIllnessCreator(): FunctionCreator {
        return { HasSevereConcomitantIllness(evaluationLabels.comorbidity) }
    }

    private fun hasHadOrganTransplantCreator(): FunctionCreator {
        return { HasHadOrganTransplant(icdModel, null, evaluationLabels.comorbidity) }
    }

    private fun hasHadOrganTransplantWithinYearsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxYearsAgo = function.param<IntegerParameter>(0).value
            val minYear = referenceDateProvider.year() - maxYearsAgo
            HasHadOrganTransplant(icdModel, minYear, evaluationLabels.comorbidity)
        }
    }

    private fun hasPotentialAbsorptionDifficultiesCreator(): FunctionCreator {
        return { HasPotentialAbsorptionDifficulties(icdModel, evaluationLabels.comorbidity) }
    }

    private fun hasContraindicationToCTCreator(): FunctionCreator {
        return { HasContraindicationToCT(icdModel, evaluationLabels.comorbidity) }
    }

    private fun hasContraindicationToMRICreator(): FunctionCreator {
        return { HasContraindicationToMRI(icdModel, evaluationLabels.comorbidity) }
    }

    private fun hasMRIScanDocumentingStableDiseaseCreator(): FunctionCreator {
        return { HasMRIScanDocumentingStableDisease(evaluationLabels.comorbidity) }
    }

    private fun hasChildPughScoreCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val requestedScores = function.param<ManyStringsParameter>(0).value
            HasChildPughScore(icdModel, requestedScores, evaluationLabels.comorbidity)
        }
    }

    private fun hasPotentialContraIndicationForStereotacticRadiosurgeryCreator(): FunctionCreator {
        return { HasPotentialContraIndicationForStereotacticRadiosurgery(evaluationLabels.comorbidity) }
    }

    private fun hasAdequateVenousAccessCreator(): FunctionCreator {
        return { HasAdequateVenousAccess(evaluationLabels.comorbidity) }
    }

    private fun hasHadComorbiditiesWithIcdCodeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val targetIcdTitles = function.param<ManyIcdTitlesParameter>(0).value
            val targetIcdCodes =
                targetIcdTitles.map { toIcdTitle(it) }
                    .map { icdModel.resolveCodeForTitle(it) ?: error("ICD code not found for title: $it") }.toSet()
            HasHadComorbidityWithIcdCode(
                icdModel,
                targetIcdCodes,
                Format.concatLowercaseWithCommaAndOr(targetIcdTitles),
                referenceDateProvider.date(),
                evaluationLabels.comorbidity
            )
        }
    }

    private fun toIcdTitle(input: String): String {
        return when {
            icdModel.isValidIcdTitle(input) -> input
            icdModel.isValidIcdCode(input) -> icdModel.resolveTitleForCodeString(input)
            else -> throw IllegalStateException("ICD title(s) or code(s) not valid: $input")
        }
    }

    private fun hasPotentialUncontrolledTumorRelatedPainCreator(): FunctionCreator {
        val medicationCategories = MedicationCategories.create(atcTree)
        val selector =
            MedicationSelector(MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider.date(), null))
        return {
            HasPotentialUncontrolledTumorRelatedPain(
                selector,
                medicationCategories.resolve("Opioids"),
                icdModel,
                evaluationLabels.comorbidity,
                evaluationLabels.medication
            )
        }
    }

    private fun hasLeptomeningealDiseaseCreator(): FunctionCreator {
        return { HasLeptomeningealDisease(icdModel, evaluationLabels.comorbidity) }
    }

    private fun hasHadComorbiditiesWithIcdCodeCreator(
        targetIcdCodes: Collection<String>,
        otherConditionTerm: String
    ): FunctionCreator {
        return {
            HasHadComorbidityWithIcdCode(
                icdModel,
                targetIcdCodes.map { IcdCode(it) }.toSet(),
                otherConditionTerm,
                referenceDateProvider.date(),
                evaluationLabels.comorbidity
            )
        }
    }

    private fun hasPotentialDisruptionOfLymphaticDrainageCreator(): FunctionCreator {
        return { HasPotentialDisruptionOfLymphaticDrainage(icdModel, evaluationLabels.comorbidity) }
    }

}
