package com.hartwig.actin.soc.evaluation.tumor

import com.hartwig.actin.soc.evaluation.FunctionCreator
import com.hartwig.actin.soc.evaluation.RuleMapper
import com.hartwig.actin.soc.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class TumorRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X to hasPrimaryTumorBelongsToDoidTermCreator(),
            EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR to hasLeftSidedColorectalTumorCreator()
        )
    }

    private fun hasPrimaryTumorBelongsToDoidTermCreator(): FunctionCreator {
        return FunctionCreator { function ->
            val doidTermToMatch: String = functionInputResolver().createOneDoidTermInput(function)
            PrimaryTumorLocationBelongsToDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToMatch)!!)
        }
    }

    private fun hasLeftSidedColorectalTumorCreator(): FunctionCreator {
        return FunctionCreator { HasLeftSidedColorectalTumor(doidModel()) }
    }
}