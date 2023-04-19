package com.hartwig.actin.soc.evaluation

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.soc.calendar.ReferenceDateProvider
import com.hartwig.actin.soc.evaluation.general.GeneralRuleMapper
import com.hartwig.actin.soc.evaluation.molecular.MolecularRuleMapper
import com.hartwig.actin.soc.evaluation.treatment.TreatmentRuleMapper
import com.hartwig.actin.soc.evaluation.tumor.TumorRuleMapper
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