package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class ComplicationRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_ANY_COMPLICATION to hasAnyComplicationCreator(),
            EligibilityRule.HAS_COMPLICATION_X to hasSpecificComplicationCreator(),
            EligibilityRule.HAS_COMPLICATION_OF_CATEGORY_X to hasComplicationOfCategoryCreator(),
            EligibilityRule.HAS_POTENTIAL_UNCONTROLLED_TUMOR_RELATED_PAIN to hasPotentialUncontrolledTumorRelatedPainCreator(),
            EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE to hasLeptomeningealDiseaseCreator(),
        )
    }

    private fun hasAnyComplicationCreator(): FunctionCreator {
        return { HasAnyComplication() }
    }

    private fun hasSpecificComplicationCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            HasSpecificComplication(termToFind)
        }
    }

    private fun hasComplicationOfCategoryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val categoryToFind = functionInputResolver().createOneStringInput(function)
            HasComplicationOfCategory(categoryToFind)
        }
    }

    private fun hasPotentialUncontrolledTumorRelatedPainCreator(): FunctionCreator {
        val interpreter: MedicationStatusInterpreter = MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date(), null)
        return { HasPotentialUncontrolledTumorRelatedPain(interpreter) }
    }

    private fun hasLeptomeningealDiseaseCreator(): FunctionCreator {
        return { HasLeptomeningealDisease() }
    }
}