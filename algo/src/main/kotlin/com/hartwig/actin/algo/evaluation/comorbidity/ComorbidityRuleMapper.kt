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
                "autoimmune disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.HEART_DISEASE_SET,
                "cardiac disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.CIRCULATORY_SYSTEM_DISEASE_CHAPTER),
                "cardiovascular disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_CONGESTIVE_HEART_FAILURE_WITH_AT_LEAST_NYHA_CLASS_X to hasHistoryOfCongestiveHeartFailureWithNYHACreator(),
            EligibilityRule.HAS_HISTORY_OF_CENTRAL_NERVOUS_SYSTEM_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.NERVOUS_SYSTEM_DISEASE_CHAPTER),
                "CNS disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_EYE_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.EYE_DISEASE_CHAPTER),
                "eye disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.DIGESTIVE_SYSTEM_DISEASE_CHAPTER),
                "gastrointestinal disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_GASTROINTESTINAL_FISTULA to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.FISTULA_OF_OESOPHAGUS,
                    IcdConstants.GASTRIC_FISTULA,
                    IcdConstants.FISTULA_OF_SMALL_INTESTINE,
                    IcdConstants.FISTULA_OF_LARGE_INTESTINE,
                    IcdConstants.FISTULA_OF_APPENDIX,
                ), "gastrointestinal fistula"
            ),
            EligibilityRule.HAS_HISTORY_OF_IMMUNODEFICIENCY to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.IMMUNO_DEFICIENCY_SET,
                "immunodeficiency"
            ),
            EligibilityRule.HAS_HISTORY_OF_INTERSTITIAL_LUNG_DISEASE_INCLUDING_PNEUMONITIS to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.INTERSTITIAL_LUNG_DISEASE_SET + IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK,
                "interstitial lung disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_LIVER_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.LIVER_DISEASE_BLOCK),
                "liver disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.RESPIRATORY_COMPROMISE_SET,
                "lung disease"
            ),
            EligibilityRule.HAS_POTENTIAL_RESPIRATORY_COMPROMISE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.RESPIRATORY_COMPROMISE_SET, "potential respiratory compromise"
            ),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.ACUTE_MYOCARDIAL_INFARCT_CODE),
                "myocardial infarct"
            ),
            EligibilityRule.HAS_HISTORY_OF_MYOCARDIAL_INFARCT_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                setOf(IcdCode(IcdConstants.ACUTE_MYOCARDIAL_INFARCT_CODE)), "myocardial infarct"
            ),
            EligibilityRule.HAS_HISTORY_OF_SPECIFIC_CONDITION_WITH_ICD_TITLE_X_WITHIN_Y_MONTHS to hasHadOtherConditionMatchingSpecificIcdTitleRecentlyCreator(),
            EligibilityRule.HAS_HISTORY_OF_PNEUMONITIS to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK,
                    IcdConstants.IDIOPATHIC_INTERSTITIAL_PNEUMONITIS_CODE,
                    IcdConstants.IDIOPATHIC_EOSINOPHILIC_PNEUMONITIS_CODE,
                    IcdConstants.RADIATION_PNEUMONITIS_CODE
                ),
                "pneumonitis"
            ),
            EligibilityRule.HAS_HISTORY_OF_STROKE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.STROKE_SET,
                "CVA"
            ),
            EligibilityRule.HAS_HISTORY_OF_STROKE_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                IcdConstants.STROKE_SET.map { IcdCode(it) }.toSet(),
                "CVA"
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT_WITHIN_X_MONTHS to hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
                IcdConstants.THROMBOEMBOLIC_EVENT_SET.map { IcdCode(it) }.toSet(),
                "thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_THROMBOEMBOLIC_EVENT to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.THROMBOEMBOLIC_EVENT_SET,
                "thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_ARTERIAL_THROMBOEMBOLIC_EVENT to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.ARTERIAL_THROMBOEMBOLIC_EVENT_SET,
                "Arterial thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_VENOUS_THROMBOEMBOLIC_EVENT to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.VENOUS_THROMBOEMBOLIC_EVENT_SET, "Venous thrombo-embolic event"
            ),
            EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.ARTERY_DISEASE_BLOCK,
                    IcdConstants.VEIN_DISEASE_BLOCK
                ), "vascular disease"
            ),
            EligibilityRule.HAS_HISTORY_OF_ULCER to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.ULCER_SET,
                "ulcer"
            ),
            EligibilityRule.HAS_HISTORY_OF_BLEEDING to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.BLEEDING_SET,
                "bleeding"
            ),
            EligibilityRule.HAS_HISTORY_OF_WOUND to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.WOUND_SET,
                "wound"
            ),
            EligibilityRule.HAS_HISTORY_OF_BONE_FRACTURE to hasHadComorbiditiesWithIcdCodeCreator(
                IcdConstants.BONE_FRACTURE_SET,
                "bone fracture"
            ),
            EligibilityRule.HAS_SEVERE_CONCOMITANT_CONDITION to hasSevereConcomitantIllnessCreator(),
            EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT to hasHadOrganTransplantCreator(),
            EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT_WITHIN_X_YEARS to hasHadOrganTransplantWithinYearsCreator(),
            EligibilityRule.HAS_GILBERT_DISEASE to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.GILBERT_SYNDROME_CODE),
                "Gilbert disease"
            ),
            EligibilityRule.HAS_HYPERTENSION to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.HYPERTENSIVE_DISEASES_BLOCK),
                "hypertension"
            ),
            EligibilityRule.HAS_HYPOTENSION to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.HYPOTENSION_BLOCK),
                "hypotension"
            ),
            EligibilityRule.HAS_DIABETES to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(IcdConstants.DIABETES_MELLITUS_BLOCK),
                "diabetes"
            ),
            EligibilityRule.HAS_INHERITED_PREDISPOSITION_TO_BLEEDING_OR_THROMBOSIS to hasInheritedPredispositionToBleedingOrThrombosisCreator(),
            EligibilityRule.HAS_POTENTIAL_ABSORPTION_DIFFICULTIES to hasPotentialAbsorptionDifficultiesCreator(),
            EligibilityRule.HAS_POTENTIAL_ORAL_MEDICATION_DIFFICULTIES to hasHadComorbiditiesWithIcdCodeCreator(
                listOf(
                    IcdConstants.FUNCTIONAL_SWALLOWING_DISORDER_CODE,
                    IcdConstants.DISORDERS_OF_ORAL_MUCOSA_CODE
                ),
                "potential oral medication difficulties",
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
                ), "renal dialysis"
            ),
            EligibilityRule.HAS_CHILD_PUGH_SCORE_X to hasChildPughScoreCreator(),
            EligibilityRule.HAS_POTENTIAL_CONTRAINDICATION_FOR_STEREOTACTIC_RADIOSURGERY to
                    hasPotentialContraIndicationForStereotacticRadiosurgeryCreator(),
            EligibilityRule.HAS_ADEQUATE_VENOUS_ACCESS to hasAdequateVenousAccessCreator(),
            EligibilityRule.MEETS_REQUIREMENTS_DURING_SIX_MINUTE_WALKING_TEST to { MeetsSixMinuteWalkingTestRequirements() },
            EligibilityRule.HAS_COMORBIDITY_WITH_ANY_ICD_TITLE_X to hasHadComorbiditiesWithIcdCodeCreator(),
            EligibilityRule.HAS_POTENTIAL_UNCONTROLLED_TUMOR_RELATED_PAIN to hasPotentialUncontrolledTumorRelatedPainCreator(),
            EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE to hasLeptomeningealDiseaseCreator(),
            EligibilityRule.HAS_SPINAL_CORD_COMPRESSION to hasHadComorbiditiesWithIcdCodeCreator(
                setOf(
                    IcdConstants.MYELOPATHY,
                    IcdConstants.TRAUMATIC_SPINAL_CORD_COMPRESSION,
                    IcdConstants.OTHER_SPECIFIED_SPINAL_CORD_DISORDERS_EXCLUDING_TRAUMA
                ),
                "spinal cord compression"
            ),
            EligibilityRule.HAS_PLEURAL_EFFUSION to
                    hasHadComorbiditiesWithIcdCodeCreator(
                        setOf(
                            IcdConstants.PLEURAL_EFFUSION_CODE,
                            IcdConstants.MALIGNANT_NEOPLASM_METASTASIS_IN_PLEURA_CODE,
                            IcdConstants.PLEURISY_CODE
                        ),
                        "pleural effusion"
                    ),
            EligibilityRule.HAS_PERITONEAL_EFFUSION to
                    hasHadComorbiditiesWithIcdCodeCreator(
                        setOf(
                            IcdConstants.MALIGNANT_NEORPLASM_METASTASIS_IN_RETROPERITONEUM_OR_PERITONEUM_BLOCK,
                            IcdConstants.ASCITES_CODE
                        ),
                        "peritoneal effusion"
                    )

        )
    }

    private fun hasInheritedPredispositionToBleedingOrThrombosisCreator(): FunctionCreator {
        return { HasInheritedPredispositionToBleedingOrThrombosis(icdModel()) }
    }

    private fun hasHadOtherConditionWithIcdCodeFromSetRecentlyCreator(
        targetIcdCodes: Set<IcdCode>,
        diseaseDescription: String
    ): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxMonthsAgo = function.param<IntegerParameter>(0).value
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong() - 1)
            HasHadOtherConditionWithIcdCodeFromSetRecently(
                icdModel(),
                targetIcdCodes,
                diseaseDescription,
                minDate,
                maxMonthsAgo
            )
        }
    }

    private fun hasHadOtherConditionMatchingSpecificIcdTitleRecentlyCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.ICD_TITLE, Parameter.Type.INTEGER)
            val icdTitle = toIcdTitle(function.param<IcdTitleParameter>(0).value)
            val maxMonthsAgo = function.param<IntegerParameter>(1).value
            val targetIcdCode = icdModel().resolveCodeForTitle(icdTitle)!!
            val minDate = referenceDateProvider().date().minusMonths(maxMonthsAgo.toLong() - 1)
            HasHadOtherConditionWithIcdCodeFromSetRecently(
                icdModel(),
                setOf(targetIcdCode),
                icdTitle,
                minDate,
                maxMonthsAgo
            )
        }
    }

    private fun hasHistoryOfCongestiveHeartFailureWithNYHACreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val nyhaClass = NyhaClass.valueOf(function.param<NyhaClassParameter>(0).value)
            HasHistoryOfCongestiveHeartFailureWithNYHA(nyhaClass, icdModel())
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
            val maxYearsAgo = function.param<IntegerParameter>(0).value
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

    private fun hasChildPughScoreCreator(): FunctionCreator {
        return { HasChildPughScore(icdModel()) }
    }

    private fun hasPotentialContraIndicationForStereotacticRadiosurgeryCreator(): FunctionCreator {
        return { HasPotentialContraIndicationForStereotacticRadiosurgery() }
    }

    private fun hasAdequateVenousAccessCreator(): FunctionCreator {
        return { HasAdequateVenousAccess() }
    }

    private fun hasHadComorbiditiesWithIcdCodeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val targetIcdTitles = function.param<ManyIcdTitlesParameter>(0).value
            val targetIcdCodes =
                targetIcdTitles.map { toIcdTitle(it) }
                    .map { icdModel().resolveCodeForTitle(it) ?: error("ICD code not found for title: $it") }.toSet()
            HasHadComorbidityWithIcdCode(
                icdModel(),
                targetIcdCodes,
                Format.concatLowercaseWithCommaAndOr(targetIcdTitles),
                referenceDateProvider().date()
            )
        }
    }

    private fun toIcdTitle(input: String): String {
        return when {
            icdModel().isValidIcdTitle(input) -> input
            icdModel().isValidIcdCode(input) -> icdModel().resolveTitleForCodeString(input)
            else -> throw IllegalStateException("ICD title(s) or code(s) not valid: $input")
        }
    }

    private fun hasPotentialUncontrolledTumorRelatedPainCreator(): FunctionCreator {
        val medicationCategories = MedicationCategories.create(atcTree())
        val selector =
            MedicationSelector(MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date(), null))
        return {
            HasPotentialUncontrolledTumorRelatedPain(
                selector,
                medicationCategories.resolve("Opioids"),
                icdModel()
            )
        }
    }

    private fun hasLeptomeningealDiseaseCreator(): FunctionCreator {
        return { HasLeptomeningealDisease(icdModel()) }
    }

    private fun hasHadComorbiditiesWithIcdCodeCreator(
        targetIcdCodes: Collection<String>,
        otherConditionTerm: String
    ): FunctionCreator {
        return {
            HasHadComorbidityWithIcdCode(
                icdModel(),
                targetIcdCodes.map { IcdCode(it) }.toSet(),
                otherConditionTerm,
                referenceDateProvider().date()
            )
        }
    }

}
