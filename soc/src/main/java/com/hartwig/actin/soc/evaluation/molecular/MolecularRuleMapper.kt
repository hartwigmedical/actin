package com.hartwig.actin.soc.evaluation.molecular

import com.hartwig.actin.soc.evaluation.FunctionCreator
import com.hartwig.actin.soc.evaluation.RuleMapper
import com.hartwig.actin.soc.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class MolecularRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X to geneHasActivatingMutationCreator(),
            EligibilityRule.WILDTYPE_OF_GENE_X to geneIsWildTypeCreator(),
            EligibilityRule.MSI_SIGNATURE to isMicrosatelliteUnstableCreator
        )
    }

    private fun geneHasActivatingMutationCreator(): FunctionCreator {
        return FunctionCreator { function ->
            GeneHasActivatingMutation(functionInputResolver().createOneGeneInput(function).geneName())
        }
    }

    private fun geneIsWildTypeCreator(): FunctionCreator {
        return FunctionCreator { function ->
            GeneIsWildType(functionInputResolver().createOneGeneInput(function).geneName())
        }
    }

    private val isMicrosatelliteUnstableCreator: FunctionCreator
        get() = FunctionCreator { function -> IsMicrosatelliteUnstable() }
}