package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.medication.MedicationCategories

class ComplicationRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_COMPLICATION_WITH_ANY_ICD_TITLE_X to hasSpecificComplicationCreator(),
            EligibilityRule.HAS_POTENTIAL_UNCONTROLLED_TUMOR_RELATED_PAIN to hasPotentialUncontrolledTumorRelatedPainCreator(),
            EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE to hasLeptomeningealDiseaseCreator(),
            EligibilityRule.HAS_PLEURAL_EFFUSION to {
                HasSpecificComplication(
                    icdModel(), setOf(
                        IcdCode(IcdConstants.PLEURAL_EFFUSION_CODE),
                        IcdCode(IcdConstants.MALIGNANT_NEOPLASM_METASTASIS_IN_PLEURA_CODE),
                        IcdCode(IcdConstants.PLEURISY_CODE)
                    )
                )
            },
            EligibilityRule.HAS_PERITONEAL_EFFUSION to {
                HasSpecificComplication(
                    icdModel(),
                    setOf(
                        IcdCode(IcdConstants.MALIGNANT_NEORPLASM_METASTASIS_IN_RETROPERITONEUM_OR_PERITONEUM_BLOCK),
                        IcdCode(IcdConstants.ASCITES_CODE)
                    )
                )
            }
        )
    }

    private fun hasSpecificComplicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val targetIcdTitles = functionInputResolver().createManyIcdTitlesInput(function)
            val targetIcdCodes = targetIcdTitles.map { icdModel().resolveCodeForTitle(it)!! }.toSet()
            HasSpecificComplication(icdModel(), targetIcdCodes)
        }
    }

    private fun hasPotentialUncontrolledTumorRelatedPainCreator(): FunctionCreator {
        val medicationCategories = MedicationCategories.create(atcTree())
        val selector = MedicationSelector(MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date(), null))
        return { HasPotentialUncontrolledTumorRelatedPain(selector, medicationCategories.resolve("Opioids"), icdModel()) }
    }

    private fun hasLeptomeningealDiseaseCreator(): FunctionCreator {
        return { HasLeptomeningealDisease(icdModel()) }
    }
}