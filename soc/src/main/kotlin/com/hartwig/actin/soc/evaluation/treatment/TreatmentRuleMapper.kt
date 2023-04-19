package com.hartwig.actin.soc.evaluation.treatment

import com.hartwig.actin.soc.evaluation.FunctionCreator
import com.hartwig.actin.soc.evaluation.RuleMapper
import com.hartwig.actin.soc.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.input.single.OneIntegerOneString
import com.hartwig.actin.treatment.input.single.TwoIntegersManyStrings
import java.time.LocalDate

class TreatmentRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_HAD_TREATMENT_NAME_X_WITHIN_Y_WEEKS to hasHadSpecificTreatmentWithinWeeksCreator(),
            EligibilityRule.HAS_HAD_COMBINED_TREATMENT_NAMES_X_AND_BETWEEN_Y_AND_Z_CYCLES to hasHadCombinedTreatmentNamesWithCyclesCreator(),
            EligibilityRule.HAS_PROGRESSIVE_DISEASE_FOLLOWING_NAME_X_TREATMENT to hasProgressiveDiseaseFollowingTreatmentNameCreator()
        )
    }

    private fun hasHadSpecificTreatmentWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: OneIntegerOneString = functionInputResolver().createOneStringOneIntegerInput(function)
            val minDate: LocalDate = referenceDateProvider().date().minusWeeks(input.integer().toLong())
            HasHadSpecificTreatmentSinceDate(input.string(), minDate)
        }
    }

    private fun hasHadCombinedTreatmentNamesWithCyclesCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val input: TwoIntegersManyStrings = functionInputResolver().createManyStringsTwoIntegersInput(function)
            HasHadCombinedTreatmentNamesWithCycles(input.strings(), input.integer1(), input.integer2())
        }
    }

    private fun hasProgressiveDiseaseFollowingTreatmentNameCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val nameToFind: String = functionInputResolver().createOneStringInput(function)
            HasHadPDFollowingSpecificTreatment(setOf(nameToFind), null)
        }
    }
}