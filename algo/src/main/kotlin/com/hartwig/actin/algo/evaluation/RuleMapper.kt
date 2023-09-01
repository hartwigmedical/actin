package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.input.FunctionInputResolver

abstract class RuleMapper(private val resources: RuleMappingResources) {

    protected fun referenceDateProvider(): ReferenceDateProvider {
        return resources.referenceDateProvider
    }

    protected fun doidModel(): DoidModel {
        return resources.doidModel
    }

    protected fun functionInputResolver(): FunctionInputResolver {
        return resources.functionInputResolver
    }

    abstract fun createMappings(): Map<EligibilityRule, FunctionCreator>
}