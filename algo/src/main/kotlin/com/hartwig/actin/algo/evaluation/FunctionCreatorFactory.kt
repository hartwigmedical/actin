package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.evaluation.general.GeneralRuleMapper
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleMapper
import com.hartwig.actin.algo.evaluation.treatment.TreatmentRuleMapper
import com.hartwig.actin.algo.evaluation.tumor.TumorRuleMapper
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.input.FunctionInputResolver

internal object FunctionCreatorFactory {

    fun create(
        referenceDateProvider: ReferenceDateProvider,
        doidModel: DoidModel, functionInputResolver: FunctionInputResolver
    ): Map<EligibilityRule, FunctionCreator> {
        val resources = RuleMappingResources(
            referenceDateProvider = referenceDateProvider, doidModel = doidModel,
            functionInputResolver = functionInputResolver
        )

        return listOf(
            GeneralRuleMapper(resources), MolecularRuleMapper(resources), TreatmentRuleMapper(resources),
            TumorRuleMapper(resources)
        )
            .map { it.createMappings() }.reduce { acc, map -> acc + map }
    }
}